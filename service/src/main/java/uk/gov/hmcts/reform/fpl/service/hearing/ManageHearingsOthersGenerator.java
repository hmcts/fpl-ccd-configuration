package uk.gov.hmcts.reform.fpl.service.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageHearingsOthersGenerator {
    private final OthersService othersService;

    public Map<String, Object> generate(CaseData caseData, HearingBooking hearingBooking) {

        List<Element<Other>> allOthers = caseData.getAllOthers();
        List<Element<Other>> selectedOthers = hearingBooking.getOthers();

        Map<String, Object> data = new HashMap<>();
        if (isNotEmpty(allOthers)) {
            data.put("hasOthers", YES.getValue());
            data.put("othersSelector",
                othersService.buildOtherSelector(unwrapElements(allOthers), unwrapElements(selectedOthers)));
            data.put("others_label", othersService.getOthersLabel(caseData.getAllOthers()));

            if (NEW_HEARING != caseData.getHearingOption()) {
                data.put("sendNoticeOfHearing", sendNoticeOfHearing(hearingBooking) ? YES.getValue() : NO.getValue());
                data.put("sendOrderToAllOthers",
                    sendOrderToAllOthers(allOthers, selectedOthers) ? YES.getValue() : NO.getValue());
            }
        }

        return data;
    }

    private boolean sendNoticeOfHearing(HearingBooking hearingBooking) {
        List<Element<Other>> hearingOthers = hearingBooking.getOthers();

        return (hearingOthers != null && !hearingOthers.isEmpty());
    }

    private boolean sendOrderToAllOthers(List<Element<Other>> allOthers, List<Element<Other>> selectedOthers) {
        return unwrapElements(allOthers).equals(unwrapElements(selectedOthers));
    }
}
