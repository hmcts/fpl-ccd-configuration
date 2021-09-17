package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Cardinality;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
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

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

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

            final Placement placement = Placement.builder()
                .childName(child.getValue().asLabel())
                .childId(child.getId())
                .build();

            placementData.setPlacement(placement);
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

        final Placement placement = Placement.builder()
            .childName(childrenList.getValueLabel())
            .childId(childrenList.getValueCodeAsUUID())
            .build();

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

        placementData.getPlacements().add(newElement(placement));

        return placementData;
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
}
