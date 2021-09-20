package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApproverBlockPrePopulatorTest {

    private static final DynamicList SELECTED_HEARING = mock(DynamicList.class);
    private static final CaseData CASE_DATA = CaseData.builder()
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersApprovedAtHearingList(SELECTED_HEARING)
            .build())
        .build();
    private static final Optional<Element<HearingBooking>> HEARING = Optional.empty();
    private static final JudgeAndLegalAdvisor JUDGE_AND_LEGAL_ADVISOR = mock(
        JudgeAndLegalAdvisor.class);

    private final HearingService hearingService = mock(HearingService.class);
    private final JudgeAndLegalAdvisorHelper judgeAndLegalAdvisorHelper = mock(JudgeAndLegalAdvisorHelper.class);

    private final ApproverBlockPrePopulator underTest = new ApproverBlockPrePopulator(
        hearingService, judgeAndLegalAdvisorHelper);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.APPROVER);
    }

    @Test
    void prePopulateWithNoHearing() {

        when(hearingService.findHearing(CASE_DATA, SELECTED_HEARING)).thenReturn(HEARING);
        when(judgeAndLegalAdvisorHelper.buildForHearing(CASE_DATA,HEARING)).thenReturn(Optional.empty());

        Map<String, Object> actual = underTest.prePopulate(CASE_DATA);

        assertThat(actual).isEqualTo(Map.of());
    }

    @Test
    void prePopulateWithSelectedHearing() {

        when(hearingService.findHearing(CASE_DATA, SELECTED_HEARING)).thenReturn(HEARING);
        when(judgeAndLegalAdvisorHelper.buildForHearing(CASE_DATA,HEARING)).thenReturn(Optional.of(
            JUDGE_AND_LEGAL_ADVISOR));

        Map<String, Object> actual = underTest.prePopulate(CASE_DATA);

        assertThat(actual).isEqualTo(Map.of(
            "judgeAndLegalAdvisor", JUDGE_AND_LEGAL_ADVISOR
        ));
    }

}
