package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.calendar.CalendarService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderService {

    private final Time time;
    private final UserService userService;
    private final CaseConverter converter;
    private final DocumentService documentService;
    private final CalendarService calendarService;
    private final DocumentSealingService sealingService;
    private final OrdersLookupService ordersLookupService;
    private final GatekeepingOrderGenerationService gatekeepingOrderGenerationService;

    public GatekeepingOrderSealDecision buildSealDecision(CaseData caseData) {
        DocumentReference order = getOrderDocument(caseData);

        return GatekeepingOrderSealDecision.builder()
            .draftDocument(order)
            .nextSteps(buildNextStepsLabel(caseData))
            .dateOfIssue(LocalDate.now())
            .orderStatus(null)
            .build();
    }

    public StandardDirectionOrder buildOrderFromUploadedFile(CaseData caseData) {
        final GatekeepingOrderSealDecision decision = caseData.getGatekeepingOrderEventData()
            .getGatekeepingOrderSealDecision();

        caseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision();
        DocumentReference draftDocument = decision.getDraftDocument();
        DocumentReference document = decision.isSealed() ? sealingService.sealDocument(draftDocument) : draftDocument;

//        if( caseData.getGatekeepingOrderEventData().getLanguageTranslationRequirement() != LanguageTranslationRequirement.NO) {
//            //TODO generate FL-PLW-LET-ENG-00748, populate, and send document for translation once FPLA-3253 is implemented
//            //TranslationRequestFormCreationService:buildTranslationRequestDocuments(templateData)
//            //Does this need moving to higher level?
//        }

        return buildBaseGatekeepingOrder(caseData).toBuilder()
            .dateOfUpload(time.now().toLocalDate())
            .uploader(userService.getUserName())
            .orderDoc(document)
            .lastUploadedOrder(decision.isSealed() ? draftDocument : null)
            .build();
    }

    public StandardDirectionOrder buildOrderFromGeneratedFile(CaseData caseData) {
        final GatekeepingOrderSealDecision decision = caseData.getGatekeepingOrderEventData()
            .getGatekeepingOrderSealDecision();

        StandardDirectionOrder currentOrder = buildBaseGatekeepingOrder(caseData);

//        if( caseData.getGatekeepingOrderEventData().getLanguageTranslationRequirement() != LanguageTranslationRequirement.NO) {
//            //TODO generate FL-PLW-LET-ENG-00748, populate, and send document for translation once FPLA-3253 is implemented
//            //TranslationRequestFormCreationService:buildTranslationRequestDocuments(templateData)
//            //Does this need moving to higher level?
//        }

        if (decision.isSealed()) {
            DocumentReference sealedDocument = buildFromDocument(generateOrder(caseData));

            return currentOrder.toBuilder()
                .dateOfIssue(formatLocalDateToString(decision.getDateOfIssue(), DATE))
                .unsealedDocumentCopy(decision.getDraftDocument())
                .orderDoc(sealedDocument)
                .build();
        } else {
            return currentOrder.toBuilder()
                .orderDoc(decision.getDraftDocument())
                .build();
        }
    }

    public Optional<HearingBooking> getHearing(CaseData caseData) {
        return caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT);
    }

    public JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge, JudgeAndLegalAdvisor issuingJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return issuingJudge.toBuilder().allocatedJudgeLabel(assignedJudgeLabel).build();
    }

    private boolean hasEnteredIssuingJudge(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (isEmpty(judgeAndLegalAdvisor)) {
            return false;
        }

        return judgeAndLegalAdvisor.isUsingAllocatedJudge() || isNotEmpty(judgeAndLegalAdvisor.getJudgeTitle());
    }

    //this constructs a label which hides the option to seal if mandatory information is missing
    //previous button can break this functionality as logic uses a hidden field (EUI-3922)
    private String buildNextStepsLabel(CaseData caseData) {
        List<String> requiredMissingInformation = new ArrayList<>();

        if (caseData.getFirstHearing().isEmpty()) {
            requiredMissingInformation.add("* the first hearing details");
        }

        if (isEmpty(caseData.getAllocatedJudge())) {
            requiredMissingInformation.add("* the allocated judge");
        }

        if (!hasEnteredIssuingJudge(caseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge())) {
            requiredMissingInformation.add("* the judge issuing the order");
        }

        if (requiredMissingInformation.isEmpty()) {
            return null;
        } else {
            final String nextStepsLabel = "## Next steps\n\n"
                + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                + "You cannot seal and send the order until adding:";
            requiredMissingInformation.add(0, nextStepsLabel);

            return String.join("\n\n", requiredMissingInformation);
        }
    }

    private StandardDirectionOrder buildBaseGatekeepingOrder(CaseData caseData) {
        GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor =
            getJudgeForTabView(eventData.getGatekeepingOrderIssuingJudge(), caseData.getAllocatedJudge());

        return StandardDirectionOrder.builder()
            .customDirections(eventData.getCustomDirections())
            .standardDirections(eventData.getStandardDirections())
            .orderStatus(defaultIfNull(eventData.getGatekeepingOrderSealDecision().getOrderStatus(), DRAFT))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();
    }

    public List<DocmosisTemplates> getNoticeOfProceedingsTemplates(CaseData caseData) {
        List<DocmosisTemplates> templates = new ArrayList<>();
        templates.add(DocmosisTemplates.C6);

        if (!caseData.getAllOthers().isEmpty()) {
            templates.add(DocmosisTemplates.C6A);
        }

        return templates;
    }

    private DocumentReference getOrderDocument(CaseData caseData) {
        final GatekeepingOrderRoute route = caseData.getGatekeepingOrderRouter();

        if (route == UPLOAD) {
            return firstNonNull(
                caseData.getReplacementSDO(),
                caseData.getPreparedSDO(),
                caseData.getGatekeepingOrderEventData().getCurrentSDO());
        }

        return buildFromDocument(generateOrder(caseData));
    }

    private Document generateOrder(CaseData caseData) {
        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);
    }

    public CaseData populateStandardDirections(CaseDetails caseDetails) {
        final CaseData caseData = converter.convert(caseDetails);
        final GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();

        final List<DirectionType> requestedDirections = eventData.getRequestedDirections();
        final List<StandardDirection> draftStandardDirections = ofNullable(caseData.getStandardDirectionOrder())
            .map(StandardDirectionOrder::getStandardDirections)
            .map(ElementUtils::unwrapElements)
            .orElseGet(ArrayList::new);

        final HearingBooking firstHearing = getHearing(caseData).orElse(null);

        Stream.of(DirectionType.values())
            .filter(directionType -> !requestedDirections.contains(directionType))
            .map(DirectionType::getFieldName)
            .forEach(caseDetails.getData()::remove);

        requestedDirections.stream()
            .map(directionType -> getCurrentStandardDirection(directionType, caseDetails)
                .orElseGet(() -> getStandardDirectionDraft(directionType, draftStandardDirections)
                    .orElseGet(() -> buildStandardDirection(directionType, firstHearing))))
            .forEach(direction -> {
                caseDetails.getData().put(direction.getType().getFieldName(), direction);
            });

        return caseData;
    }

    public CaseData updateStandardDirections(CaseDetails caseDetails) {
        final CaseData caseData = converter.convert(caseDetails);
        final GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();

        final HearingBooking firstHearing = getHearing(caseData).orElse(null);

        final List<StandardDirection> standardDirections = eventData.getRequestedDirections().stream()
            .map(requestedType -> {
                StandardDirection standardDirection = converter
                    .convert(caseDetails.getData().get(requestedType.getFieldName()), StandardDirection.class);

                DirectionConfiguration directionConfig = ordersLookupService.getDirectionConfiguration(requestedType);

                if (standardDirection.getDueDateType() == DirectionDueDateType.DAYS) {
                    standardDirection.setDateToBeCompletedBy(
                        calculateDirectionDueDate(firstHearing, directionConfig.getDisplay(),
                            standardDirection.getDaysBeforeHearing()));
                }

                return standardDirection.applyConfig(directionConfig);
            })
            .collect(toList());

        eventData.setStandardDirections(wrapElements(standardDirections));

        return caseData;
    }

    private Optional<StandardDirection> getCurrentStandardDirection(DirectionType type, CaseDetails caseDetails) {
        return ofNullable(converter.convert(caseDetails.getData().get(type.getFieldName()), StandardDirection.class));
    }

    private Optional<StandardDirection> getStandardDirectionDraft(DirectionType type, List<StandardDirection> draft) {
        return draft.stream()
            .filter(draftedDirection -> Objects.equals(draftedDirection.getType(), type))
            .findFirst();
    }

    private StandardDirection buildStandardDirection(DirectionType type, HearingBooking hearing) {
        final DirectionConfiguration directionConfig = ordersLookupService.getDirectionConfiguration(type);

        return StandardDirection.builder()
            .dateToBeCompletedBy(calculateDirectionDueDate(hearing, directionConfig.getDisplay()))
            .dueDateType(DAYS)
            .build()
            .applyConfig(directionConfig);
    }

    private LocalDateTime calculateDirectionDueDate(HearingBooking hearing, Display display) {
        return calculateDirectionDueDate(hearing, display, null);
    }

    private LocalDateTime calculateDirectionDueDate(HearingBooking hearing, Display display, Integer workingDays) {

        final LocalDate hearingDay = ofNullable(hearing)
            .map(HearingBooking::getStartDate)
            .map(LocalDateTime::toLocalDate)
            .orElse(null);

        if (hearingDay == null) {
            return null;
        }

        final Integer daysBefore = Optional.ofNullable(workingDays).orElse(Optional.ofNullable(display.getDelta())
            .map(Integer::parseInt)
            .orElse(0));

        LocalDate deadline = daysBefore == 0 ? hearingDay : calendarService.getWorkingDayFrom(hearingDay, daysBefore);

        return LocalDateTime.of(deadline, LocalTime.parse(defaultIfNull(display.getTime(), "00:00:00")));
    }
}
