package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationAdded;
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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementService {

    private static final List<PlacementSupportingDocument.Type> REQUIRED_SUPPORTING_DOCS =
        List.of(BIRTH_ADOPTION_CERTIFICATE, STATEMENT_OF_FACTS);
    private static final List<PlacementConfidentialDocument.Type> REQUIRED_CONFIDENTAIL_DOCS = List.of(ANNEX_B);

    private final Time time;
    private final FeeService feeService;
    private final PbaNumberService pbaNumberService;
    private final DocumentSealingService sealingService;

    public PlacementEventData prepareChildren(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final List<Element<Child>> childrenWithoutPlacement = getChildrenWithoutPlacement(caseData);

        placementData.setPlacementChildrenCardinality(Cardinality.from(childrenWithoutPlacement.size()));

        if (placementData.getPlacementChildrenCardinality() == ONE) {
            final Element<Child> child = childrenWithoutPlacement.get(0);

            placementData.setPlacement(getChildPlacement(child));

            placementData.setPlacementNoticeForFirstParentParentsList(respondentsList(caseData));
            placementData.setPlacementNoticeForSecondParentParentsList(respondentsList(caseData));
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

        placementData.setPlacement(getChildPlacement(child));

        placementData.setPlacementNoticeForFirstParentParentsList(respondentsList(caseData));
        placementData.setPlacementNoticeForSecondParentParentsList(respondentsList(caseData));

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

        if (ofNullable(placement.getApplication()).map(DocumentReference::getBinaryUrl).isEmpty()) {
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

        final Placement placement = placementData.getPlacement();

        final DocumentReference applicationDocument = Optional.ofNullable(placement)
            .map(Placement::getApplication)
            .orElseThrow(() -> new IllegalStateException("Missing placement application document"));

        placement.setApplication(sealingService.sealDocument(applicationDocument));
        placement.setPlacementUploadDateTime(time.now());

        List<PlacementNoticeDocument> noticeDocuments = new ArrayList<>();

        if (YES == placementData.getPlacementNoticeForLocalAuthorityRequired()) {

            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForLocalAuthority())
                .noticeDescription(placementData.getPlacementNoticeForLocalAuthorityDescription())
                .recipientName("Local authority")
                .type(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY);

            if (YES == placementData.getPlacementNoticeResponseFromLocalAuthorityReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromLocalAuthority())
                    .responseDescription(placementData.getPlacementNoticeResponseFromLocalAuthorityDescription());
            }

            noticeDocuments.add(noticeBuilder.build());
        }

        if (YES == placementData.getPlacementNoticeForCafcassRequired()) {
            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForCafcass())
                .noticeDescription(placementData.getPlacementNoticeForCafcassDescription())
                .recipientName("Cafcass")
                .type(PlacementNoticeDocument.RecipientType.CAFCASS);

            if (YES == placementData.getPlacementNoticeResponseFromCafcassReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromCafcass())
                    .responseDescription(placementData.getPlacementNoticeResponseFromCafcassDescription());
            }

            noticeDocuments.add(noticeBuilder.build());
        }

        if (YES == placementData.getPlacementNoticeForFirstParentRequired()) {

            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForFirstParent())
                .noticeDescription(placementData.getPlacementNoticeForFirstParentDescription())
                .recipientName(placementData.getPlacementNoticeForFirstParentParentsList().getValueLabel())
                .respondentID(placementData.getPlacementNoticeForFirstParentParentsList().getValueCodeAsUUID())
                .type(PlacementNoticeDocument.RecipientType.PARENT_FIRST);

            if (YES == placementData.getPlacementNoticeResponseFromFirstParentReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromFirstParent())
                    .responseDescription(placementData.getPlacementNoticeResponseFromFirstParentDescription());
            }

            noticeDocuments.add(noticeBuilder.build());
        }

        if (YES == placementData.getPlacementNoticeForSecondParentRequired()) {
            PlacementNoticeDocument.PlacementNoticeDocumentBuilder noticeBuilder = PlacementNoticeDocument.builder()
                .notice(placementData.getPlacementNoticeForSecondParent())
                .noticeDescription(placementData.getPlacementNoticeForSecondParentDescription())
                .recipientName(placementData.getPlacementNoticeForSecondParentParentsList().getValueLabel())
                .respondentID(placementData.getPlacementNoticeForSecondParentParentsList().getValueCodeAsUUID())
                .type(PlacementNoticeDocument.RecipientType.PARENT_SECOND);

            if (YES == placementData.getPlacementNoticeResponseFromSecondParentReceived()) {
                noticeBuilder.response(placementData.getPlacementNoticeResponseFromSecondParent())
                    .responseDescription(placementData.getPlacementNoticeResponseFromSecondParentDescription());
            }

            noticeDocuments.add(noticeBuilder.build());
        }

        placement.setNoticeDocuments(wrapElements(noticeDocuments));

        placementData.getPlacements().add(newElement(placement));

        return placementData;
    }

    public List<Object> getEvents(CaseData caseData) {

        final List<Object> events = new ArrayList<>();

        unwrapElements(caseData.getPlacementEventData().getPlacement().getNoticeDocuments())
            .stream()
            .map(notice -> PlacementNoticeAdded.builder()
                .caseData(caseData)
                .notice(notice)
                .build())
            .collect(Collectors.toCollection(() -> events));

        events.add(new PlacementApplicationAdded(caseData));

        return events;
    }

    private boolean isPaymentRequired(PlacementEventData eventData) {

        return ofNullable(eventData)
            .map(PlacementEventData::getPlacementLastPaymentTime)
            .map(LocalDateTime::toLocalDate)
            .map(lastPayment -> !lastPayment.isEqual(time.now().toLocalDate()))
            .orElse(true);
    }

    private List<Element<Child>> getChildrenWithoutPlacement(CaseData caseData) {
        final PlacementEventData eventData = caseData.getPlacementEventData();

        final List<UUID> childrenWithPlacementIds = eventData.getPlacements()
            .stream()
            .map(placement -> placement.getValue().getChildId())
            .collect(toList());

        return caseData.getAllChildren().stream()
            .filter(child -> !childrenWithPlacementIds.contains(child.getId()))
            .collect(toList());
    }

    private Placement getChildPlacement(Element<Child> child) {

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
                    .build())
            )
            .build();
    }

    private DynamicList respondentsList(CaseData caseData) {
        final Function<Respondent, String> stringifier = respondent -> format("%s - %s",
            respondent.getParty().getFullName(),
            respondent.getParty().getRelationshipToChild());

        return asDynamicList(caseData.getAllRespondents(), stringifier);
    }
}
