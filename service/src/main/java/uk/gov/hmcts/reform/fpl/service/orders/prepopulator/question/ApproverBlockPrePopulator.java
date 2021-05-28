package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApproverBlockPrePopulator implements QuestionBlockOrderPrePopulator {

    private static final String CASE_FIELD_KEY = "judgeAndLegalAdvisor";

    private final HearingService hearingService;
    private final JudgeAndLegalAdvisorHelper judgeAndLegalAdvisorHelper;

    @Override
    public OrderQuestionBlock accept() {
        return APPROVER;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {

        DynamicList selectedHearing = caseData.getManageOrdersEventData().getManageOrdersApprovedAtHearingList();
        Optional<Element<HearingBooking>> hearing = hearingService.findHearing(caseData, selectedHearing);


        Optional<JudgeAndLegalAdvisor> judgeAndLegalAdvisor =
            judgeAndLegalAdvisorHelper.buildForHearing(caseData, hearing);

        return judgeAndLegalAdvisor
            .map(it -> Map.<String, Object>of(CASE_FIELD_KEY, it))
            .orElse(Map.of());

    }
}
