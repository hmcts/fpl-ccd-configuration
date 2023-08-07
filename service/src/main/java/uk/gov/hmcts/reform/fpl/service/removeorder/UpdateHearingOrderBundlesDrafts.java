package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class UpdateHearingOrderBundlesDrafts {

    public void update(CaseDetailsMap data,
                       List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts,
                       Element<HearingOrdersBundle> selectedHearingOrderBundle) {
        List<Element<HearingOrdersBundle>> updatedHearingOrderBundle =
                getUpdatedHearingOrderBundle(hearingOrdersBundlesDrafts,
                    selectedHearingOrderBundle);

        data.putIfNotEmpty("hearingOrdersBundlesDrafts", updatedHearingOrderBundle);
    }

    public void update(Element<HearingOrdersBundle> selectedHearingOrderBundle,
                       Supplier<List<Element<HearingOrdersBundle>>> hearingOrdersBundles,
                       Consumer<List<Element<HearingOrdersBundle>>> updateCaseDetails) {
        List<Element<HearingOrdersBundle>> updatedHearingOrderBundle =
                getUpdatedHearingOrderBundle(hearingOrdersBundles.get(),
                        selectedHearingOrderBundle);
        updateCaseDetails.accept(updatedHearingOrderBundle);
    }

    private List<Element<HearingOrdersBundle>> getUpdatedHearingOrderBundle(
            List<Element<HearingOrdersBundle>> hearingOrdersBundles,
            Element<HearingOrdersBundle> selectedHearingOrderBundle) {

        List<Element<HearingOrdersBundle>> updatedHearingOrderBundle;
        if (selectedHearingOrderBundle.getValue().getOrders().isEmpty()) {
            updatedHearingOrderBundle = new ArrayList<>(hearingOrdersBundles);
            updatedHearingOrderBundle.removeIf(bundle -> bundle.getId().equals(selectedHearingOrderBundle.getId()));
        } else {
            updatedHearingOrderBundle = hearingOrdersBundles.stream()
                .map(hearingOrdersBundleElement -> {
                    if (selectedHearingOrderBundle.getId().equals(hearingOrdersBundleElement.getId())) {
                        return selectedHearingOrderBundle;
                    }

                    return hearingOrdersBundleElement;
                }).toList();
        }
        return updatedHearingOrderBundle;
    }
}
