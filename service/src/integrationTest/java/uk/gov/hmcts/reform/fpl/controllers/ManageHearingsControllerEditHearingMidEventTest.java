package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ManageHearingsControllerEditHearingMidEventTest extends ManageHearingsControllerTest {
    private static String ERROR_MESSAGE = "There are no relevant hearings to change.";

    ManageHearingsControllerEditHearingMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPopulatePreviousVenueFieldsWhenUserSelectsAddNewHearing() {
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(3)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(5)));
        Element<HearingBooking> futureHearing = element(testHearing(now().plusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(NEW_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2, futureHearing))
            .build();

        CaseData updatedCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertThat(updatedCaseData.getPreviousHearingVenue()).isEqualTo(PreviousHearingVenue.builder()
            .previousVenue("Aberdeen Tribunal Hearing Centre, 48 Huntly Street, AB1, Aberdeen, AB10 1SH")
            .build());
    }

    @Test
    void shouldBuildHearingDateListAndResetFirstHearingFlagWhenNonFirstHearingSelected() {
        Element<HearingBooking> hearing1 = element(testHearing(now().plusDays(2)));
        Element<HearingBooking> hearing2 = element(testHearing(now().plusDays(3)).toBuilder()
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue(hearing1.getValue().getVenue())
                .build())
            .build());

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(hearing2.getId())
            .hearingDetails(List.of(hearing1, hearing2))
            .build();

        CaseData updatedCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertThat(updatedCaseData.getHearingDateList()).isEqualTo(dynamicList(hearing2.getId(), hearing1, hearing2));
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
        assertHearingCaseFields(updatedCaseData, hearing2.getValue());
    }

    @Test
    void shouldBuildHearingDateListAndSetFirstHearingFlagWhenFirstHearingSelected() {
        Element<HearingBooking> hearing1 = element(testHearing(now().plusDays(2)));
        Element<HearingBooking> hearing2 = element(testHearing(now().plusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(hearing1.getId())
            .hearingDetails(List.of(hearing1, hearing2))
            .build();

        CaseData updatedCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertThat(updatedCaseData.getHearingDateList()).isEqualTo(dynamicList(hearing1.getId(), hearing1, hearing2));
        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo("Yes");
        assertHearingCaseFields(updatedCaseData, hearing1.getValue());
    }

    @Test
    void shouldBuildPastHearingDateListWhenHearingIsAdjourned() {
        Element<HearingBooking> futureHearing1 = element(testHearing(now().plusDays(2)));
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));
        Element<HearingBooking> futureHearing2 = element(testHearing(now().plusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(HearingOptions.ADJOURN_HEARING)
            .hearingDetails(List.of(futureHearing1, pastHearing1, pastHearing2, futureHearing2))
            .pastAndTodayHearingDateList(pastHearing1.getId())
            .build();

        CaseData updatedCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertThat(updatedCaseData.getPastAndTodayHearingDateList())
            .isEqualTo(dynamicList(pastHearing1.getId(), pastHearing1, pastHearing2));
    }

    @Test
    void shouldPopulateHearingToBeReListedFromSelectedCancelledHearing() {
        Element<HearingBooking> cancelledHearing = element(
            testHearing(now().plusDays(2), "96", VACATED_TO_BE_RE_LISTED));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(RE_LIST_HEARING)
            .toReListHearingDateList(dynamicList(cancelledHearing.getId(), cancelledHearing))
            .cancelledHearingDetails(List.of(cancelledHearing))
            .build();

        CaseData currentCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertCurrentHearingReListedFrom(currentCaseData, cancelledHearing.getValue());
    }

    @Test
    void shouldBuildHearingDateListWhenHearingIsVacated() {
        Element<HearingBooking> futureHearing1 = element(testHearing(now().plusDays(2)));
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));
        Element<HearingBooking> futureHearing2 = element(testHearing(now().plusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(HearingOptions.VACATE_HEARING)
            .hearingDetails(List.of(futureHearing1, pastHearing1, pastHearing2, futureHearing2))
            .toVacateHearingDateList(futureHearing1.getId())
            .build();

        CaseData updatedCaseData = extractCaseData(postEditHearingMidEvent(initialCaseData));

        assertThat(updatedCaseData.getToVacateHearingDateList())
            .isEqualTo(dynamicList(futureHearing1.getId(), futureHearing1, pastHearing1, pastHearing2, futureHearing2));
    }

    @Test
    void shouldReturnErrorsWhenEditingAHearingButNoFutureHearingsExist() {
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postEditHearingMidEvent(initialCaseData);

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenAdjourningAHearingButNoPastOrCurrentHearingsExist() {
        Element<HearingBooking> futureHearing1 = element(testHearing(now().plusDays(2)));
        Element<HearingBooking> futureHearing2 = element(testHearing(now().plusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .hearingDetails(List.of(futureHearing1, futureHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postEditHearingMidEvent(initialCaseData);

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenVacatingAHearingButNoFutureOrCurrentHearingsExist() {
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(VACATE_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postEditHearingMidEvent(initialCaseData);

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorWhenNoHearingToRelistExists() {
        Element<HearingBooking> pastHearing1 = element(testHearing(now().minusDays(2)));
        Element<HearingBooking> pastHearing2 = element(testHearing(now().minusDays(3)));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(RE_LIST_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postEditHearingMidEvent(initialCaseData);

        assertThat(response.getErrors()).contains("There are no adjourned or vacated hearings to re-list");
    }

    private static void assertHearingCaseFields(CaseData caseData, HearingBooking hearingBooking) {
        assertThat(caseData.getHearingType()).isEqualTo(hearingBooking.getType());
        assertThat(caseData.getHearingStartDate()).isEqualTo(hearingBooking.getStartDate());
        assertThat(caseData.getHearingEndDate()).isEqualTo(hearingBooking.getEndDate());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(hearingBooking.getJudgeAndLegalAdvisor());
        assertThat(caseData.getPreviousHearingVenue()).isEqualTo(hearingBooking.getPreviousHearingVenue());
    }

    AboutToStartOrSubmitCallbackResponse postEditHearingMidEvent(CaseData caseData) {
        return postMidEvent(caseData, "edit-hearing");
    }
}
