package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UpdateHearingOrderBundlesDrafts {

    public void update(CaseDetailsMap data,
                       List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts,
                       Element<HearingOrdersBundle> selectedHearingOrderBundle) {
        List<Element<HearingOrdersBundle>> updatedHearingOrderBundle;

        if (selectedHearingOrderBundle.getValue().getOrders().isEmpty()) {
            updatedHearingOrderBundle = new ArrayList<>(hearingOrdersBundlesDrafts);
            updatedHearingOrderBundle.removeIf(bundle -> bundle.getId().equals(selectedHearingOrderBundle.getId()));
        } else {
            updatedHearingOrderBundle = hearingOrdersBundlesDrafts.stream()
                .map(hearingOrdersBundleElement -> {
                    if (selectedHearingOrderBundle.getId().equals(hearingOrdersBundleElement.getId())) {
                        return selectedHearingOrderBundle;
                    }

                    return hearingOrdersBundleElement;
                }).collect(Collectors.toList());
        }

        data.putIfNotEmpty("hearingOrdersBundlesDrafts", updatedHearingOrderBundle);
    }
}
