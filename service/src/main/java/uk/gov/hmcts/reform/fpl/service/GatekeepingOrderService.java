package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN_IMMEDIATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ARRANGE_INTERPRETERS_IMMEDIATE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.UDO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
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
            .dateOfIssue(time.now().toLocalDate())
            .orderStatus(null)
            .build();
    }

    public GatekeepingOrderSealDecision buildSealedDecision(CaseData caseData) {
        DocumentReference order = getOrderDocument(caseData);

        return GatekeepingOrderSealDecision.builder()
            .draftDocument(order)
            .dateOfIssue(time.now().toLocalDate())
            .orderStatus(SEALED)
            .build();
    }

    public StandardDirectionOrder buildOrderFromUploadedFile(CaseData caseData) {
        var eventData = caseData.getGatekeepingOrderEventData();
        final GatekeepingOrderSealDecision decision = eventData.getGatekeepingOrderSealDecision();

        DocumentReference document = decision.getDraftDocument();

        LanguageTranslationRequirement translationRequirements =
            eventData.getGatekeepingTranslationRequirements();
        return buildBaseGatekeepingOrder(caseData).toBuilder()
            .dateOfUpload(time.now().toLocalDate())
            .uploader(userService.getUserName())
            .orderDoc(document)
            .translationRequirements(translationRequirements)
            .build();
    }

    public StandardDirectionOrder buildOrderFromGeneratedFile(CaseData caseData) {

        final GatekeepingOrderSealDecision decision =
            caseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision();

        StandardDirectionOrder currentOrder = buildBaseGatekeepingOrder(caseData);

        if (decision.isSealed()) {

            var docmosisTemplate = nonNull(caseData.getGatekeepingOrderRouter()) ? SDO : UDO;
            DocumentReference sealedDocument = buildFromDocument(generateOrder(caseData, docmosisTemplate));
            return currentOrder.toBuilder()
                .dateOfIssue(formatLocalDateToString(decision.getDateOfIssue(), DATE))
                .unsealedDocumentCopy(decision.getDraftDocument())
                .orderDoc(sealedDocument)
                .translationRequirements(translateToWelshIfCaseRequired(caseData))
                .build();
        } else {
            return currentOrder.toBuilder()
                .orderDoc(decision.getDraftDocument())
                .build();
        }
    }

    private LanguageTranslationRequirement translateToWelshIfCaseRequired(CaseData caseData) {
        return YesNo.fromString(caseData.getLanguageRequirement()) == YesNo.YES ? ENGLISH_TO_WELSH : NO;
    }

    public Optional<HearingBooking> getHearing(CaseData caseData) {
        return caseData.getFirstHearingOfTypes(List.of(HearingType.CASE_MANAGEMENT,
            HearingType.INTERIM_CARE_ORDER, HearingType.ACCELERATED_DISCHARGE_OF_CARE));
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

        if (!caseData.getOthersV2().isEmpty()) {
            templates.add(DocmosisTemplates.C6A);
        }

        return templates;
    }

    private DocumentReference getOrderDocument(CaseData caseData) {

        final GatekeepingOrderRoute sdoRouter;
        final DocmosisTemplates docmosisTemplate;
        if (nonNull(caseData.getGatekeepingOrderRouter())) {
            sdoRouter = caseData.getGatekeepingOrderRouter();
            docmosisTemplate = SDO;
        } else {
            sdoRouter = caseData.getUrgentDirectionsRouter();
            docmosisTemplate = UDO;
        }

        if (sdoRouter == UPLOAD) {
            return firstNonNull(
                caseData.getReplacementSDO(),
                caseData.getPreparedSDO(),
                caseData.getGatekeepingOrderEventData().getCurrentSDO());
        }
        return buildFromDocument(generateOrder(caseData, docmosisTemplate));
    }

    private Document generateOrder(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, docmosisTemplate);
    }

    public CaseData populateStandardDirections(CaseDetails caseDetails) {
        final CaseData caseData = converter.convert(caseDetails);
        final GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();
        var order = nonNull(caseData.getGatekeepingOrderRouter())
            ? caseData.getStandardDirectionOrder() : caseData.getUrgentDirectionsOrder();

        final List<StandardDirection> draftStandardDirections = ofNullable(order)
            .map(StandardDirectionOrder::getStandardDirections)
            .map(ElementUtils::unwrapElements)
            .orElseGet(ArrayList::new);

        final List<DirectionType> requestedDirections = eventData.getRequestedDirections();

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
        final var eventData = caseData.getGatekeepingOrderEventData();

        final HearingBooking firstHearing = getHearing(caseData).orElse(null);

        final List<StandardDirection> standardDirections = eventData.getRequestedDirections().stream()
            .map(requestedType -> {
                StandardDirection standardDirection = converter
                    .convert(caseDetails.getData().get(requestedType.getFieldName()), StandardDirection.class);

                if (standardDirection != null) {
                    DirectionConfiguration directionConfig = ordersLookupService
                        .getDirectionConfiguration(requestedType);

                    if (standardDirection.getDueDateType() == DirectionDueDateType.DAYS) {
                        standardDirection.setDateToBeCompletedBy(
                            calculateDirectionDueDate(firstHearing, directionConfig.getDisplay(),
                                standardDirection.getDaysBeforeHearing()));
                    }

                    return standardDirection.applyConfig(directionConfig);
                } else {
                    // DFPL-1381 quick fix for null pointer exception
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(toList());

        eventData.setStandardDirections(wrapElements(standardDirections));

        return caseData;
    }

    public StandardDirectionOrder sealDocumentAfterEventSubmitted(CaseData caseData) {
        StandardDirectionOrder order = (nonNull(caseData.getStandardDirectionOrder()))
            ? caseData.getStandardDirectionOrder() : caseData.getUrgentDirectionsOrder();

        if (caseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision().isSealed()) {
            DocumentReference orderDoc = order.getOrderDoc();

            StandardDirectionOrder sealedOrder = buildBaseGatekeepingOrder(caseData).toBuilder()
                .dateOfUpload(order.getDateOfUpload())
                .uploader(order.getUploader())
                .orderDoc(sealingService.sealDocument(orderDoc, caseData.getCourt(), caseData.getSealType()))
                .lastUploadedOrder(orderDoc)
                .translationRequirements(order.getTranslationRequirements())
                .build();
            return sealedOrder;
        } else {
            return order;
        }
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
        final boolean isImmediateStandardDirection =
            APPOINT_CHILDREN_GUARDIAN_IMMEDIATE.equals(type) || ARRANGE_INTERPRETERS_IMMEDIATE.equals(type);
        final int defaultDaysBeforeHearing = 2;

        return StandardDirection.builder()
            .type(directionConfig.getType())
            .title(directionConfig.getTitle())
            .assignee(directionConfig.getAssignee())
            .description(directionConfig.getText())
            .dateToBeCompletedBy(
                isImmediateStandardDirection
                    ? null
                    : calculateDirectionDueDate(hearing, directionConfig.getDisplay())
            )
            .dueDateType(isImmediateStandardDirection ? null : DAYS)
            .daysBeforeHearing(isImmediateStandardDirection ? null : defaultDaysBeforeHearing)
            .build();
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

        final int daysBefore = Optional.ofNullable(workingDays).orElse(Optional.ofNullable(display.getDelta())
            .map(Integer::parseInt)
            .orElse(0));

        LocalDate deadline = daysBefore == 0 ? hearingDay : calendarService.getWorkingDayFrom(hearingDay, daysBefore);

        LocalTime deadlineTime =
            LocalTime.parse(
                Optional.of(hearing)
                    .map(HearingBooking::getStartDate)
                    .map(startDate -> startDate.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .orElse("00:00:00")
            );

        return LocalDateTime.of(deadline, deadlineTime);

    }
}
