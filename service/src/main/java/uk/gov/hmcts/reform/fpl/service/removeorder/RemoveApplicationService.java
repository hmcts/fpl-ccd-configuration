package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationRemovalReason.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveApplicationService {

    public DynamicList buildDynamicList(CaseData caseData) {
        return buildDynamicList(caseData, null);
    }

    public DynamicList buildDynamicList(CaseData caseData, UUID selected) {
        List<Element<AdditionalApplicationsBundle>> applications = defaultIfNull(
            caseData.getAdditionalApplicationsBundle(), new ArrayList<>());

        applications.sort(Comparator
            .comparing((Element<AdditionalApplicationsBundle> bundle) -> bundle.getValue().getUploadedDateTime()));

        return asDynamicList(applications, selected, AdditionalApplicationsBundle::toLabel);
    }

    public void populateApplicationFields(CaseDetailsMap data, AdditionalApplicationsBundle application) {
        data.put("applicationTypeToBeRemoved", application.toLabel());
        data.put("c2ApplicationToBeRemoved", defaultIfNull(application.getC2DocumentBundle(),
            C2DocumentBundle.builder().build()).getDocument());
        data.put("otherApplicationToBeRemoved", defaultIfNull(application.getOtherApplicationsBundle(),
            OtherApplicationsBundle.builder().build()).getDocument());
        data.put("orderDateToBeRemoved", application.getUploadedDateTime());
    }

    public Element<AdditionalApplicationsBundle> getRemovedApplicationById(CaseData caseData, UUID selectedBundleId) {
        return caseData.getAdditionalApplicationsBundle().stream()
            .filter(orderElement -> selectedBundleId.equals(orderElement.getId()))
            .findAny()
            .orElseThrow(() -> new RemovableOrderOrApplicationNotFoundException(selectedBundleId));
    }

    public void removeApplicationFromCase(CaseData caseData, CaseDetailsMap caseDetailsMap, UUID removedApplicationId) {
        Element<AdditionalApplicationsBundle> bundleElement = getRemovedApplicationById(caseData, removedApplicationId);

        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();
        for (int i = 0; i < orders.size(); i++) {
            Element<GeneratedOrder> order = orders.get(i);
            if (bundleElement.getId().toString().equals(order.getValue().getLinkedApplicationId())) {
                order = element(order.getId(), order.getValue().toBuilder().linkedApplicationId(null).build());
                orders.set(i, order);
            }
        }

        caseData.getAdditionalApplicationsBundle().remove(bundleElement);
        caseDetailsMap.putIfNotEmpty("additionalApplicationsBundle", caseData.getAdditionalApplicationsBundle());

        List<Element<AdditionalApplicationsBundle>> hiddenApplications = caseData.getHiddenApplicationsBundle();
        bundleElement.getValue().setRemovalReason(getReasonToRemove(caseData));
        hiddenApplications.add(bundleElement);
        caseDetailsMap.put("hiddenApplicationsBundle", hiddenApplications);
    }

    public Optional<AdditionalApplicationsBundle> getRemovedApplications(
        List<Element<AdditionalApplicationsBundle>> hiddenApplications,
        List<Element<AdditionalApplicationsBundle>> previousHiddenApplications
    ) {
        if (!Objects.equals(hiddenApplications, previousHiddenApplications)) {
            return hiddenApplications.stream()
                .filter(order -> !previousHiddenApplications.contains(order))
                .findFirst()
                .map(Element::getValue);
        }
        return Optional.empty();
    }

    public String getApplicationFee(AdditionalApplicationsBundle removedApplication) {
        String fee = removedApplication.getAmountToPay();
        Optional<BigDecimal> decimalAmount = fromCCDMoneyGBP(fee);
        String refundFeeText;

        if (decimalAmount.isPresent()) {
            BigDecimal amountToDisplay = decimalAmount.get();
            refundFeeText = "An application fee of £" + amountToDisplay + " needs to be refunded.";
        } else {
            refundFeeText = "An application fee needs to be refunded.";
        }

        return refundFeeText;
    }

    public String getApplicantName(AdditionalApplicationsBundle removedApplication) {
        if (!isEmpty(removedApplication.getC2DocumentBundle())) {
            return removedApplication.getC2DocumentBundle().getApplicantName();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
            return removedApplication.getOtherApplicationsBundle().getApplicantName();
        }

        return "";
    }

    public String getFilename(AdditionalApplicationsBundle removedApplication) {
        String c2DocumentName = "";
        String otherDocumentName = "";
        if (!isEmpty(removedApplication.getC2DocumentBundle())) {
            c2DocumentName = removedApplication.getC2DocumentBundle().getDocument().getFilename();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
            otherDocumentName = removedApplication.getOtherApplicationsBundle().getDocument().getFilename();
        }

        return Stream.of(c2DocumentName, otherDocumentName)
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(", "));
    }

    private String getReasonToRemove(CaseData caseData) {
        if (caseData.getReasonToRemoveApplication() == OTHER) {
            return caseData.getApplicationRemovalDetails();
        } else {
            return caseData.getReasonToRemoveApplication().toString();
        }
    }

}
