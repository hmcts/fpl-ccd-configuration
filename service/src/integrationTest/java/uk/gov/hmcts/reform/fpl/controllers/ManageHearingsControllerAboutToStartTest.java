package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ManageHearingsControllerAboutToStartTest extends ManageHearingsControllerTest {

    ManageHearingsControllerAboutToStartTest() {
        super("manage-hearings");
    }

    @Test
    void shouldSetFirstHearingFlagAndDefaultPreAttendanceWhenHearingsEmpty() {

        CaseData initialCaseData = CaseData.builder().build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(initialCaseData));

        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo(YES.getValue());
        assertThat(updatedCaseData.getPreHearingAttendanceDetails()).isEqualTo("1 hour before the hearing");
    }

    @Test
    void shouldNotSetFirstHearingFlagAndDefaultPreAttendanceWhenHearingsPresent() {

        CaseData initialCaseData = CaseData.builder()
            .hearingDetails(wrapElements(TestDataHelper.testHearing()))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(initialCaseData));

        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo(NO.getValue());
        assertThat(updatedCaseData.getPreHearingAttendanceDetails()).isNull();
    }

    @Test
    void shouldSetJudgeWhenAllocatedJudgePresent() {

        CaseData initialCaseData = CaseData.builder()
            .id(nextLong())
            .allocatedJudge(Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Richards")
                .judgeEmailAddress("richards@example.com")
                .build())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(initialCaseData));

        assertThat(updatedCaseData.getJudgeAndLegalAdvisor()).isEqualTo(JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Richards").build());
    }

    @Test
    void shouldSetHearingsDetails() {
        Element<HearingBooking> futureHearing1 = element(testHearing(now().plusDays(3)));
        Element<HearingBooking> futureHearing2 = element(testHearing(now().plusDays(3)));
        Element<HearingBooking> todayHearing = element(testHearing(now()));
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .id(nextLong())
            .hearingDetails(List.of(futureHearing1, futureHearing2, todayHearing, pastHearing1, pastHearing2))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(initialCaseData));

        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo("No");
        assertThat(updatedCaseData.getHasExistingHearings()).isEqualTo("Yes");
        assertThat(updatedCaseData.getHearingDateList())
            .isEqualTo(dynamicList(futureHearing1, futureHearing2));
        assertThat(updatedCaseData.getPastAndTodayHearingDateList())
            .isEqualTo(dynamicList(todayHearing, pastHearing1, pastHearing2));
        assertThat(updatedCaseData.getFutureAndTodayHearingDateList())
            .isEqualTo(dynamicList(futureHearing1, futureHearing2, todayHearing));
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
    }

    @Test
    void shouldSetOtherFieldsWhenOthersPresent() {
        CaseData initialCaseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(Other.builder().name("real").build())
                .additionalOthers(wrapElements(Other.builder().name("second").build()))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(initialCaseData);

        assertThat(response.getData()).containsKeys("hasOthers", "othersSelector", "others_label");
    }

    @Test
    void shouldNotSetOtherFieldsWhenOthersPresent() {
        CaseData initialCaseData = CaseData.builder().build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(initialCaseData);


        assertThat(response.getData()).doesNotContainKeys("hasOthers", "others_label");
        assertThat(response.getData()).containsEntry("othersSelector", null);
    }
}
