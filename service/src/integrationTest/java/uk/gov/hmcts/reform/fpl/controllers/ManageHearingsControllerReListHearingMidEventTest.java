package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerReListHearingMidEventTest extends AbstractControllerTest {

    ManageHearingsControllerReListHearingMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPrePopulateNewReListedHearingWithAdjournedHearingDetails() {
        Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
        Element<HearingBooking> otherHearing = element(randomHearing());

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .pastAndTodayHearingDateList(hearingToBeAdjourned.getId())
            .hearingDetails(List.of(otherHearing, hearingToBeAdjourned))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "re-list"));

        assertThat(updatedCaseData.getHearingStartDate()).isNull();
        assertThat(updatedCaseData.getHearingEndDate()).isNull();
        assertThat(updatedCaseData.getPreviousHearingVenue()).isNull();

        assertThat(updatedCaseData.getHearingType())
            .isEqualTo(hearingToBeAdjourned.getValue().getType());
        assertThat(updatedCaseData.getJudgeAndLegalAdvisor())
            .isEqualTo(hearingToBeAdjourned.getValue().getJudgeAndLegalAdvisor());
        assertThat(updatedCaseData.getHearingVenue())
            .isEqualTo(hearingToBeAdjourned.getValue().getVenue());
    }

    private static HearingBooking randomHearing() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venue("96")
            .build();
    }

}
