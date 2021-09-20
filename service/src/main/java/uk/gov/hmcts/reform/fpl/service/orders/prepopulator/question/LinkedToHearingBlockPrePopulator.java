package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LinkedToHearingBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    public static final String MANAGE_ORDERS_APPROVED_AT_HEARING_LIST = "manageOrdersApprovedAtHearingList";

    private final HearingService hearingService;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.LINKED_TO_HEARING;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        List<Element<HearingBooking>> onlyHearingsInPast =
            hearingService.findOnlyHearingsTodayOrInPastNonVacated(caseData);

        return Map.of(
            MANAGE_ORDERS_APPROVED_AT_HEARING_LIST, caseData.buildDynamicHearingList(onlyHearingsInPast, null)
        );
    }
}
