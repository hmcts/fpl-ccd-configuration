package uk.gov.hmcts.reform.fpl.service.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageHearingsOthersGenerator {
    private final OthersService othersService;
    private final FeatureToggleService toggleService;

    public Map<String, Object> generate(CaseData caseData, HearingBooking hearingBooking) {

        List<Element<Other>> allOthers = caseData.getAllOthers();
        List<Element<Other>> selectedOthers = hearingBooking.getOthers();

        Map<String, Object> data = new HashMap<>();

        if (toggleService.isServeOrdersAndDocsToOthersEnabled()) {
            if (!allOthers.isEmpty()) {
                data.put("hasOthers", YES.getValue());
                data.put("othersSelector", buildOtherSelector(allOthers, selectedOthers));
                data.put("others_label", othersService.getOthersLabel(caseData.getAllOthers()));
                data.put("sendOrderToAllOthers",
                    sendOrderToAllOthers(allOthers, selectedOthers) ? YES.getValue() : NO.getValue());
            }
        }

        return data;
    }

    private Selector buildOtherSelector(List<Element<Other>> allOthers, List<Element<Other>> selectedOthers) {
        List<Integer> selected = new ArrayList<>();

        if (selectedOthers != null) {
            for (int i = 0; i < allOthers.size(); i++) {
                if (unwrapElements(selectedOthers).contains(allOthers.get(i).getValue())) {
                    selected.add(i);
                }
            }
        }

        return Selector.builder().selected(selected).build().setNumberOfOptions(allOthers.size());
    }

    private boolean sendOrderToAllOthers(List<Element<Other>> allOthers, List<Element<Other>> selectedOthers) {
        return unwrapElements(allOthers).equals(unwrapElements(selectedOthers));
    }
}
