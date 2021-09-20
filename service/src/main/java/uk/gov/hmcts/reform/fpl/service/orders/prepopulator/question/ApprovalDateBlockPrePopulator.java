package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApprovalDateBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private static final String CASE_FIELD_KEY = "manageOrdersApprovalDate";

    private final HearingService hearingService;
    private final Time time;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.APPROVAL_DATE;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        if (hasDataAlreadySet(caseData)) {
            return Map.of();
        }

        DynamicList selectedHearing = caseData.getManageOrdersEventData().getManageOrdersApprovedAtHearingList();
        Optional<Element<HearingBooking>> hearing = hearingService.findHearing(caseData, selectedHearing);

        if (hearing.isPresent()) {
            return Map.of(
                CASE_FIELD_KEY, hearing.get().getValue().getEndDate().toLocalDate()
            );
        }

        return Map.of(CASE_FIELD_KEY, time.now().toLocalDate());
    }

    private boolean hasDataAlreadySet(CaseData caseData) {
        return caseData.getManageOrdersEventData().getManageOrdersApprovalDate() != null;
    }
}
