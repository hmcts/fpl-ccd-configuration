package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

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
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;
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
    private final RespondentService respondentService;

    public PlacementEventData prepareChildren(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final List<Element<Child>> childrenWithoutPlacement = caseData.getAllChildren();

        placementData.setPlacementChildrenCardinality(Cardinality.from(childrenWithoutPlacement.size()));

        if (placementData.getPlacementChildrenCardinality() == ONE) {
            final Element<Child> child = childrenWithoutPlacement.get(0);

            placementData.setPlacement(getChildPlacement(placementData, child));

            flattenNotices(caseData);
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

        return flattenNotices(caseData);
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

        currentPlacement.setNoticeDocuments(getListOfNotices(placementData));

        currentPlacement.setPlacementRespondentsToNotify(
            respondentService.getSelectedRespondents(caseData, caseData.getSendPlacementNoticeToAllRespondents())
        );

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
            events.add(new PlacementApplicationChanged(caseData, placement));
        } else {
            log.info("No changes in placement application");
            return emptyList();
        }

        final List<PlacementNoticeDocument> noticeDocuments = unwrapElements(placement.getNoticeDocuments());
        final List<PlacementNoticeDocument> noticeDocumentsBefore = placementBefore
            .map(Placement::getNoticeDocuments)
            .map(ElementUtils::unwrapElements)
            .orElse(emptyList());

        final PlacementNoticeDocument localAuthorityNotice = getNotice(noticeDocuments, LOCAL_AUTHORITY);
        final PlacementNoticeDocument localAuthorityNoticeBefore = getNotice(noticeDocumentsBefore, LOCAL_AUTHORITY);

        final PlacementNoticeDocument cafcassNotice = getNotice(noticeDocuments, CAFCASS);
        final PlacementNoticeDocument cafcassNoticeBefore = getNotice(noticeDocumentsBefore, CAFCASS);

        final PlacementNoticeDocument firstParentNotice = getNotice(noticeDocuments, PARENT_FIRST);
        final PlacementNoticeDocument firstParentNoticeBefore = getNotice(noticeDocumentsBefore, PARENT_FIRST);

        final PlacementNoticeDocument secondParentNotice = getNotice(noticeDocuments, PARENT_SECOND);
        final PlacementNoticeDocument secondParentNoticeBefore = getNotice(noticeDocumentsBefore, PARENT_SECOND);

        if (nonNull(localAuthorityNotice) && !localAuthorityNotice.equals(localAuthorityNoticeBefore)) {
            events.add(new PlacementNoticeChanged(caseData, placement, localAuthorityNotice));
        }

        if (nonNull(cafcassNotice) && !cafcassNotice.equals(cafcassNoticeBefore)) {
            events.add(new PlacementNoticeChanged(caseData, placement, cafcassNotice));
        }

        if (nonNull(firstParentNotice) && !firstParentNotice.equals(firstParentNoticeBefore)
            && !firstParentNotice.equals(secondParentNoticeBefore)) {

            events.add(new PlacementNoticeChanged(caseData, placement, firstParentNotice));
        }

        if (nonNull(secondParentNotice) && !secondParentNotice.equals(secondParentNoticeBefore)
            && !secondParentNotice.equals(firstParentNoticeBefore)) {

            events.add(new PlacementNoticeChanged(caseData, placement, secondParentNotice));
        }

        return events;
    }

    private PlacementNoticeDocument getNotice(List<PlacementNoticeDocument> notices, RecipientType type) {
        return notices.stream()
            .filter(notice -> Objects.equals(notice.getType(), type))
            .findFirst()
            .orElse(null);
    }

    private UUID getNoticeId(List<Element<PlacementNoticeDocument>> notices, RecipientType type) {
        return defaultIfNull(notices, new ArrayList<Element<PlacementNoticeDocument>>())
            .stream()
            .filter(notice -> Objects.equals(notice.getValue().getType(), type))
            .map(Element::getId)
            .findFirst()
            .orElse(null);
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

    private DynamicList respondentsList(CaseData caseData) {
        return respondentsList(caseData, null);
    }

    private DynamicList respondentsList(CaseData caseData, UUID selected) {
        final Function<Respondent, String> stringifier = respondent -> format("%s - %s",
            respondent.getParty().getFullName(),
            respondent.getParty().getRelationshipToChild());

        return asDynamicList(caseData.getAllRespondents(), selected, stringifier);
    }

    private PlacementEventData flattenNotices(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final Placement placement = placementData.getPlacement();

        final Optional<PlacementNoticeDocument> localAuthorityNotice = findPlacementNotice(placement, LOCAL_AUTHORITY);
        final Optional<PlacementNoticeDocument> cafcassNotice = findPlacementNotice(placement, CAFCASS);
        final Optional<PlacementNoticeDocument> firstParentNotice = findPlacementNotice(placement, PARENT_FIRST);
        final Optional<PlacementNoticeDocument> secondParentNotice = findPlacementNotice(placement, PARENT_SECOND);

        if (localAuthorityNotice.isPresent()) {
            placementData.setPlacementNoticeForLocalAuthorityRequired(YES);
            placementData.setPlacementNoticeForLocalAuthority(localAuthorityNotice
                .get().getNotice());
            placementData.setPlacementNoticeForLocalAuthorityDescription(localAuthorityNotice
                .get().getNoticeDescription());
            placementData.setPlacementNoticeResponseFromLocalAuthorityReceived(YesNo.from(nonNull(localAuthorityNotice
                .get().getResponse())));
            placementData.setPlacementNoticeResponseFromLocalAuthority(localAuthorityNotice
                .get().getResponse());
            placementData.setPlacementNoticeResponseFromLocalAuthorityDescription(localAuthorityNotice
                .get().getResponseDescription());
        } else {
            placementData.setPlacementNoticeForLocalAuthorityRequired(NO);
            placementData.setPlacementNoticeResponseFromLocalAuthorityReceived(NO);
        }

        if (cafcassNotice.isPresent()) {
            placementData.setPlacementNoticeForCafcassRequired(YES);
            placementData.setPlacementNoticeForCafcass(cafcassNotice
                .get().getNotice());
            placementData.setPlacementNoticeForCafcassDescription(cafcassNotice
                .get().getNoticeDescription());
            placementData.setPlacementNoticeResponseFromCafcassReceived(YesNo.from(nonNull(cafcassNotice
                .get().getResponse())));
            placementData.setPlacementNoticeResponseFromCafcass(cafcassNotice
                .get().getResponse());
            placementData.setPlacementNoticeResponseFromCafcassDescription(cafcassNotice
                .get().getResponseDescription());
        } else {
            placementData.setPlacementNoticeForCafcassRequired(NO);
            placementData.setPlacementNoticeResponseFromCafcassReceived(NO);
        }

        if (firstParentNotice.isPresent()) {
            placementData.setPlacementNoticeForFirstParentRequired(YES);
            placementData.setPlacementNoticeForFirstParent(firstParentNotice
                .get().getNotice());
            placementData.setPlacementNoticeForFirstParentDescription(firstParentNotice
                .get().getNoticeDescription());
            placementData.setPlacementNoticeResponseFromFirstParentReceived(YesNo.from(nonNull(firstParentNotice
                .get().getResponse())));
            placementData.setPlacementNoticeResponseFromFirstParent(firstParentNotice
                .get().getResponse());
            placementData.setPlacementNoticeResponseFromFirstParentDescription(firstParentNotice
                .get().getResponseDescription());

            placementData.setPlacementNoticeForFirstParentParentsList(respondentsList(caseData, firstParentNotice
                .get().getRespondentId()));
        } else {
            placementData.setPlacementNoticeForFirstParentRequired(NO);
            placementData.setPlacementNoticeResponseFromFirstParentReceived(NO);
            placementData.setPlacementNoticeForFirstParentParentsList(respondentsList(caseData));
        }

        if (secondParentNotice.isPresent()) {
            placementData.setPlacementNoticeForSecondParentRequired(YES);
            placementData.setPlacementNoticeForSecondParent(secondParentNotice
                .get().getNotice());
            placementData.setPlacementNoticeForSecondParentDescription(secondParentNotice
                .get().getNoticeDescription());
            placementData.setPlacementNoticeResponseFromSecondParentReceived(YesNo.from(nonNull(secondParentNotice
                .get().getResponse())));
            placementData.setPlacementNoticeResponseFromSecondParent(secondParentNotice
                .get().getResponse());
            placementData.setPlacementNoticeResponseFromSecondParentDescription(secondParentNotice
                .get().getResponseDescription());

            placementData.setPlacementNoticeForSecondParentParentsList(respondentsList(caseData, secondParentNotice
                .get().getRespondentId()));
        } else {
            placementData.setPlacementNoticeForSecondParentRequired(NO);
            placementData.setPlacementNoticeResponseFromSecondParentReceived(NO);
            placementData.setPlacementNoticeForSecondParentParentsList(respondentsList(caseData));
        }

        return placementData;
    }

    private List<Element<PlacementNoticeDocument>> getListOfNotices(PlacementEventData placementData) {


        final List<Element<PlacementNoticeDocument>> oldNotices = placementData.getPlacement().getNoticeDocuments();

        final List<Element<PlacementNoticeDocument>> noticeDocuments = new ArrayList<>();

        if (YES == placementData.getPlacementNoticeForLocalAuthorityRequired()) {

            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForLocalAuthority())
                .noticeDescription(placementData.getPlacementNoticeForLocalAuthorityDescription())
                .recipientName("Local authority")
                .type(LOCAL_AUTHORITY);

            if (YES == placementData.getPlacementNoticeResponseFromLocalAuthorityReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromLocalAuthority())
                    .responseDescription(placementData.getPlacementNoticeResponseFromLocalAuthorityDescription());
            }

            noticeDocuments.add(element(getNoticeId(oldNotices, LOCAL_AUTHORITY), noticeBuilder.build()));
        }

        if (YES == placementData.getPlacementNoticeForCafcassRequired()) {
            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForCafcass())
                .noticeDescription(placementData.getPlacementNoticeForCafcassDescription())
                .recipientName("Cafcass")
                .type(CAFCASS);

            if (YES == placementData.getPlacementNoticeResponseFromCafcassReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromCafcass())
                    .responseDescription(placementData.getPlacementNoticeResponseFromCafcassDescription());
            }

            noticeDocuments.add(element(getNoticeId(oldNotices, CAFCASS), noticeBuilder.build()));
        }

        if (YES == placementData.getPlacementNoticeForFirstParentRequired()) {

            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForFirstParent())
                .noticeDescription(placementData.getPlacementNoticeForFirstParentDescription())
                .recipientName(placementData.getPlacementNoticeForFirstParentParentsList().getValueLabel())
                .respondentId(placementData.getPlacementNoticeForFirstParentParentsList().getValueCodeAsUUID())
                .type(PARENT_FIRST);

            if (YES == placementData.getPlacementNoticeResponseFromFirstParentReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromFirstParent())
                    .responseDescription(placementData.getPlacementNoticeResponseFromFirstParentDescription());
            }

            noticeDocuments.add(element(getNoticeId(oldNotices, PARENT_FIRST), noticeBuilder.build()));
        }

        if (YES == placementData.getPlacementNoticeForSecondParentRequired()) {
            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForSecondParent())
                .noticeDescription(placementData.getPlacementNoticeForSecondParentDescription())
                .recipientName(placementData.getPlacementNoticeForSecondParentParentsList().getValueLabel())
                .respondentId(placementData.getPlacementNoticeForSecondParentParentsList().getValueCodeAsUUID())
                .type(PARENT_SECOND);

            if (YES == placementData.getPlacementNoticeResponseFromSecondParentReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromSecondParent())
                    .responseDescription(placementData.getPlacementNoticeResponseFromSecondParentDescription());
            }

            noticeDocuments.add(element(getNoticeId(oldNotices, PARENT_SECOND), noticeBuilder.build()));
        }

        return noticeDocuments;
    }

    private Optional<PlacementNoticeDocument> findPlacementNotice(Placement placement,
                                                                  PlacementNoticeDocument.RecipientType type) {

        if (isNull(placement)) {
            return Optional.empty();
        }

        return unwrapElements(placement.getNoticeDocuments()).stream()
            .filter(placementNotice -> Objects.equals(placementNotice.getType(), type))
            .findFirst();
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
