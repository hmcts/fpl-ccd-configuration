package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfPlacementHearing;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.A92;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementService {

    private static final List<PlacementSupportingDocument.Type> REQUIRED_SUPPORTING_DOCS =
        List.of(BIRTH_ADOPTION_CERTIFICATE, STATEMENT_OF_FACTS);
    private static final List<PlacementConfidentialDocument.Type> REQUIRED_CONFIDENTAIL_DOCS = List.of(ANNEX_B);

    private final Time time;
    private final FeeService feeService;
    private final PbaNumberService pbaNumberService;
    private final DocumentSealingService sealingService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public PlacementEventData prepareChildren(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final List<Element<Child>> childrenWithoutPlacement = caseData.getAllChildren();

        placementData.setPlacementChildrenCardinality(Cardinality.from(childrenWithoutPlacement.size()));

        if (placementData.getPlacementChildrenCardinality() == ONE) {
            final Element<Child> child = childrenWithoutPlacement.get(0);

            placementData.setPlacement(getChildPlacement(placementData, child));
        }

        if (placementData.getPlacementChildrenCardinality() == MANY) {
            placementData.setPlacementChildrenList(asDynamicList(childrenWithoutPlacement, Child::asLabel));
        }

        return placementData;
    }

    public PlacementEventData preparePlacement(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final DynamicList childrenList = placementData.getPlacementChildrenList();

        if (isNull(childrenList) || isNull(childrenList.getValueCodeAsUUID())) {
            throw new IllegalStateException("Child for placement application not selected");
        }

        final Element<Child> child = getElement(childrenList.getValueCodeAsUUID(), caseData.getAllChildren());

        placementData.setPlacement(getChildPlacement(placementData, child));

        return placementData;
    }

    public PlacementEventData preparePlacementFromExisting(CaseData caseData) {
        final PlacementEventData placementData = caseData.getPlacementEventData();
        final Placement placement = getPlacementById(caseData, caseData.getPlacementList().getValueCodeAsUUID());
        placementData.setPlacement(placement);

        return placementData;
    }

    public List<String> checkDocuments(CaseData caseData) {

        final Placement placement = caseData.getPlacementEventData().getPlacement();

        final Set<PlacementSupportingDocument.Type> supportingTypes =
            unwrapElements(placement.getSupportingDocuments()).stream()
                .map(PlacementSupportingDocument::getType)
                .collect(toSet());

        final Set<PlacementConfidentialDocument.Type> confidentialTypes =
            unwrapElements(placement.getConfidentialDocuments())
                .stream()
                .map(PlacementConfidentialDocument::getType)
                .collect(toSet());

        final List<String> errors = new ArrayList<>();

        if (NO.equals(placement.isSubmitted()) && ofNullable(placement.getApplication())
            .map(DocumentReference::getBinaryUrl).isEmpty()) {
            errors.add("Add required placement application");
        }

        REQUIRED_SUPPORTING_DOCS.stream().filter(type -> !supportingTypes.contains(type))
            .map(type -> format("Add required %s supporting document", type.getName()))
            .collect(toCollection(() -> errors));

        REQUIRED_CONFIDENTAIL_DOCS.stream().filter(type -> !confidentialTypes.contains(type))
            .map(type -> format("Add required %s confidential document", type.getName()))
            .collect(toCollection(() -> errors));

        return errors;
    }

    public List<String> checkNotices(CaseData caseData) {

        final List<String> errors = new ArrayList<>();

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final Optional<UUID> firstParentId = ofNullable(placementData.getPlacementNoticeForFirstParentParentsList())
            .map(DynamicList::getValueCodeAsUUID);

        final Optional<UUID> secondParentId = ofNullable(placementData.getPlacementNoticeForSecondParentParentsList())
            .map(DynamicList::getValueCodeAsUUID);

        if (firstParentId.isPresent() && secondParentId.isPresent() && firstParentId.equals(secondParentId)) {
            errors.add("First and second parents can not be same");
        }

        return errors;
    }

    public List<String> checkPayment(CaseData caseData) {

        final PBAPayment pbaPayment = Optional.ofNullable(caseData.getPlacementEventData())
            .map(PlacementEventData::getPlacementPayment)
            .orElseThrow(() -> new IllegalStateException("Missing placement payment details"));

        pbaPayment.setPbaNumber(pbaNumberService.update(pbaPayment.getPbaNumber()));

        return pbaNumberService.validate(pbaPayment.getPbaNumber());
    }

    public PlacementEventData preparePayment(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final boolean isPaymentRequired = isPaymentRequired(placementData);

        placementData.setPlacementPaymentRequired(YesNo.from(isPaymentRequired));

        if (isPaymentRequired) {
            final FeesData feesData = feeService.getFeesDataForPlacement();
            placementData.setPlacementFee(toCCDMoneyGBP(feesData.getTotalAmount()));
        }

        return placementData;
    }

    public PlacementEventData savePlacement(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final Placement currentPlacement = placementData.getPlacement();

        final Optional<Element<Placement>> existingPlacement = placementData.getPlacements().stream()
            .filter(pl -> Objects.equals(pl.getValue().getChildId(), currentPlacement.getChildId()))
            .findFirst();

        currentPlacement.setPlacementNotice(placementData.getPlacementNotice());

        if (existingPlacement.isPresent()) {
            existingPlacement.get().setValue(currentPlacement);
        } else {

            final DocumentReference applicationDocument = currentPlacement.getApplication();

            if (isNull(applicationDocument)) {
                throw new IllegalStateException("Missing placement application document");
            }

            currentPlacement.setApplication(sealingService.sealDocument(applicationDocument, SealType.ENGLISH));

            currentPlacement.setPlacementUploadDateTime(time.now());

            placementData.getPlacements().add(newElement(currentPlacement));
        }

        return placementData;
    }

    public PlacementEventData generateA92(CaseData caseData) {
        final PlacementEventData placementEventData = caseData.getPlacementEventData();
        final Optional<Element<Child>> child = caseData.getAllChildren().stream().filter(
            element -> element.getId().equals(placementEventData.getPlacement().getChildId())
        ).findFirst();

        if (child.isEmpty()) {
            throw new IllegalStateException("Placement child not present in case data");
        }

        final Child placementChild = child.get().getValue();

        DocmosisNoticeOfPlacementHearing hearing = DocmosisNoticeOfPlacementHearing.builder()
            .child(DocmosisChild.builder()
                .name(placementChild.getParty().getFullName())
                .dateOfBirth(formatLocalDateToString(placementChild.getParty().getDateOfBirth(), DATE))
                .gender(placementChild.getParty().getGender())
                .build())
            .courtName(caseData.getCourt().getName())
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearingDate(formatLocalDateTimeBaseUsingFormat(placementEventData.getPlacementNoticeDateTime(), DATE_TIME_WITH_ORDINAL_SUFFIX)
                .formatted(getDayOfMonthSuffix(placementEventData.getPlacementNoticeDateTime().getDayOfMonth())))
            .hearingDuration(placementEventData.getPlacementNoticeDuration())
            .hearingVenue(hearingVenueLookUpService.getHearingVenue(placementEventData.getPlacementNoticeVenue()).getVenue())
            .postingDate(formatLocalDateToString(time.now().toLocalDate(), DATE))
            .build();

        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(hearing, A92, RenderFormat.PDF);
        Document document = uploadDocumentService.uploadDocument(docmosisDocument.getBytes(),
            A92.getDocumentTitle(time.now().toLocalDate()), RenderFormat.PDF.getMediaType());
        placementEventData.setPlacementNotice(DocumentReference.buildFromDocument(document));
        return placementEventData;    }


    public List<Object> getEvents(CaseData caseData, CaseData caseDataBefore) {

        final List<Object> events = new ArrayList<>();

        final PlacementEventData placementData = caseData.getPlacementEventData();
        final PlacementEventData placementDataBefore = caseDataBefore.getPlacementEventData();

        final UUID childId = placementData.getPlacement().getChildId();

        final Placement placement = findChildPlacement(placementData, childId).orElseThrow();
        final Optional<Placement> placementBefore = findChildPlacement(placementDataBefore, childId);

        if (placementBefore.isEmpty()) {
            events.add(new PlacementApplicationSubmitted(caseData, placement));
        } else if (!placement.equals(placementBefore.get())) {
            if (!placementBefore.get().getPlacementNotice().equals(placement.getPlacementNotice())) {
                // Placement notice changed
                events.add(new PlacementNoticeAdded(caseData, placement));
            } else {
                // Something else updated
                events.add(new PlacementApplicationChanged(caseData, placement));
            }
        } else {
            log.info("No changes in placement application");
            return emptyList();
        }

        return events;
    }

    private boolean isPaymentRequired(PlacementEventData eventData) {

        return ofNullable(eventData)
            .map(PlacementEventData::getPlacementLastPaymentTime)
            .map(LocalDateTime::toLocalDate)
            .map(lastPayment -> !lastPayment.isEqual(time.now().toLocalDate()))
            .orElse(true);
    }

    private Placement getChildPlacement(PlacementEventData eventData, Element<Child> child) {

        return findChildPlacement(eventData, child.getId()).orElseGet(() -> getDefaultPlacement(child));
    }

    private Optional<Placement> findChildPlacement(PlacementEventData eventData, UUID childId) {

        final List<Element<Placement>> placements = Optional.ofNullable(eventData)
            .map(PlacementEventData::getPlacements)
            .orElse(emptyList());

        return placements.stream()
            .map(Element::getValue)
            .filter(placement -> Objects.equals(placement.getChildId(), childId))
            .findFirst();
    }

    private Placement getDefaultPlacement(Element<Child> child) {

        return Placement.builder()
            .childName(child.getValue().asLabel())
            .childId(child.getId())
            .confidentialDocuments(wrapElements(PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .build()))
            .supportingDocuments(wrapElements(
                PlacementSupportingDocument.builder()
                    .type(BIRTH_ADOPTION_CERTIFICATE)
                    .build(),
                PlacementSupportingDocument.builder()
                    .type(STATEMENT_OF_FACTS)
                    .build()))
            .build();
    }

    private DynamicList respondentsList(CaseData caseData, UUID selected) {
        final Function<Respondent, String> stringifier = respondent -> format("%s - %s",
            respondent.getParty().getFullName(),
            respondent.getParty().getRelationshipToChild());

        return asDynamicList(caseData.getAllRespondents(), selected, stringifier);
    }

    public List<Element<String>> getPlacements(CaseData caseData) {
        return caseData.getPlacementEventData().getPlacements().stream()
            .map(element -> {
                UUID placementId = element.getId();
                Element<Child> child = getChildByPlacementId(caseData, placementId);
                String childName = child.getValue().asLabel();
                return element(placementId, childName);
            }).collect(Collectors.toList());
    }

    public Placement getPlacementById(CaseData caseData, UUID placementId) {
        return findElement(placementId, caseData.getPlacementEventData().getPlacements())
            .map(Element::getValue)
            .orElseThrow();
    }

    public Element<Child> getChildByPlacementId(CaseData caseData, UUID placementId) {
        Placement placement = getPlacementById(caseData, placementId);
        UUID childId = placement.getChildId();
        return findElement(childId, caseData.getAllChildren()).orElseThrow();
    }

}
