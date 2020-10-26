package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerAboutToStartTest extends AbstractControllerTest {

    ManageHearingsControllerAboutToStartTest() {
        super("manage-hearings");
    }

    @Test
    void shouldSetFirstHearingFlagWhenHearingsEmpty() {

        CaseData initialCaseData = CaseData.builder()
            .id(nextLong())
            .selectedHearingId(randomUUID())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(initialCaseData));

        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo(YES.getValue());
        assertThat(updatedCaseData.getHasExistingHearings()).isNull();
        assertThat(updatedCaseData.getSelectedHearingId()).isNull();
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
    void shouldSetHearingDynamicLists() {
        Element<HearingBooking> futureHearing1 = hearingFromToday(3);
        Element<HearingBooking> futureHearing2 = hearingFromToday(2);
        Element<HearingBooking> todayHearing = hearingFromToday(0);
        Element<HearingBooking> pastHearing1 = hearingFromToday(-2);
        Element<HearingBooking> pastHearing2 = hearingFromToday(-3);

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
    }

    @SafeVarargs
    private Object dynamicList(Element<HearingBooking>... hearings) {
        DynamicList dynamicList = ElementUtils.asDynamicList(Arrays.asList(hearings), HearingBooking::toLabel);
        return mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {
        });
    }

    private static Element<HearingBooking> hearingFromToday(int daysFromToday) {
        final LocalDateTime startTime = LocalDateTime.now().plusDays(daysFromToday);
        return element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startTime)
            .endDate(startTime.plusDays(1))
            .build());
    }
}
