package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerEditHearingMidEventTest extends AbstractControllerTest {
    private static String ERROR_MESSAGE = "There are no relevant hearings to change.";

    ManageHearingsControllerEditHearingMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPopulatePreviousVenueFieldsWhenUserSelectsAddNewHearing() {
        Element<HearingBooking> pastHearing1 = element(hearing(now().minusDays(3), "96"));
        Element<HearingBooking> pastHearing2 = element(hearing(now().minusDays(5), "298"));
        Element<HearingBooking> futureHearing = element(hearing(now().plusDays(3), "162"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(NEW_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2, futureHearing))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "edit-hearing"));

        assertThat(updatedCaseData.getPreviousHearingVenue()).isEqualTo(PreviousHearingVenue.builder()
            .previousVenue("Aberdeen Tribunal Hearing Centre, 48 Huntly Street, AB1, Aberdeen, AB10 1SH")
            .build());
    }

    @Test
    void shouldBuildHearingDateListAndResetFirstHearingFlagWhenNonFirstHearingSelected() {
        Element<HearingBooking> hearing1 = element(hearing(now().plusDays(2), "162"));
        Element<HearingBooking> hearing2 = element(hearing(now().plusDays(3), "166").toBuilder()
            .previousHearingVenue(PreviousHearingVenue.builder()
                .previousVenue(hearing1.getValue().getVenue())
                .build())
            .build());

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(hearing2.getId())
            .hearingDetails(List.of(hearing1, hearing2))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "edit-hearing"));

        assertThat(updatedCaseData.getHearingDateList()).isEqualTo(dynamicList(hearing2.getId(), hearing1, hearing2));
        assertThat(updatedCaseData.getFirstHearingFlag()).isNull();
        assertHearingCaseFields(updatedCaseData, hearing2.getValue());
    }

    @Test
    void shouldBuildHearingDateListAndSetFirstHearingFlagWhenFirstHearingSelected() {
        Element<HearingBooking> hearing1 = element(hearing(now().plusDays(2), "162"));
        Element<HearingBooking> hearing2 = element(hearing(now().plusDays(3), "166"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDateList(hearing1.getId())
            .hearingDetails(List.of(hearing1, hearing2))
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "edit-hearing"));

        assertThat(updatedCaseData.getHearingDateList()).isEqualTo(dynamicList(hearing1.getId(), hearing1, hearing2));
        assertThat(updatedCaseData.getFirstHearingFlag()).isEqualTo("Yes");
        assertHearingCaseFields(updatedCaseData, hearing1.getValue());
    }

    @Test
    void shouldBuildPastHearingDateListWhenHearingIsAdjourned() {
        Element<HearingBooking> futureHearing1 = element(hearing(now().plusDays(2), "162"));
        Element<HearingBooking> pastHearing1 = element(hearing(now().minusDays(2), "96"));
        Element<HearingBooking> pastHearing2 = element(hearing(now().minusDays(3), "298"));
        Element<HearingBooking> futureHearing2 = element(hearing(now().plusDays(3), "166"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(HearingOptions.ADJOURN_HEARING)
            .hearingDetails(List.of(futureHearing1, pastHearing1, pastHearing2, futureHearing2))
            .pastAndTodayHearingDateList(pastHearing1.getId())
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "edit-hearing"));

        assertThat(updatedCaseData.getPastAndTodayHearingDateList())
            .isEqualTo(dynamicList(pastHearing1.getId(), pastHearing1, pastHearing2));
    }

    @Test
    void shouldBuildFutureHearingDateListWhenHearingIsVacated() {
        Element<HearingBooking> futureHearing1 = element(hearing(now().plusDays(2), "162"));
        Element<HearingBooking> pastHearing1 = element(hearing(now().minusDays(2), "96"));
        Element<HearingBooking> pastHearing2 = element(hearing(now().minusDays(3), "298"));
        Element<HearingBooking> futureHearing2 = element(hearing(now().plusDays(3), "166"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(HearingOptions.VACATE_HEARING)
            .hearingDetails(List.of(futureHearing1, pastHearing1, pastHearing2, futureHearing2))
            .futureAndTodayHearingDateList(futureHearing1.getId())
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(initialCaseData, "edit-hearing"));

        assertThat(updatedCaseData.getFutureAndTodayHearingDateList())
            .isEqualTo(dynamicList(futureHearing1.getId(), futureHearing1, futureHearing2));
    }

    @Test
    void shouldReturnErrorsWhenEditingAHearingButNoFutureHearingsExist() {
        Element<HearingBooking> pastHearing1 = element(hearing(now().minusDays(2), "96"));
        Element<HearingBooking> pastHearing2 = element(hearing(now().minusDays(3), "298"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(EDIT_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(initialCaseData, "edit-hearing");

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenAdjourningAHearingButNoPastOrCurrentHearingsExist() {
        Element<HearingBooking> futureHearing1 = element(hearing(now().plusDays(2), "162"));
        Element<HearingBooking> futureHearing2 = element(hearing(now().plusDays(3), "166"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(ADJOURN_HEARING)
            .hearingDetails(List.of(futureHearing1, futureHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(initialCaseData, "edit-hearing");

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenVacatingAHearingButNoFutureOrCurrentHearingsExist() {
        Element<HearingBooking> pastHearing1 = element(hearing(now().minusDays(2), "96"));
        Element<HearingBooking> pastHearing2 = element(hearing(now().minusDays(3), "298"));

        CaseData initialCaseData = CaseData.builder()
            .hearingOption(VACATE_HEARING)
            .hearingDetails(List.of(pastHearing1, pastHearing2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(initialCaseData, "edit-hearing");

        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    private static void assertHearingCaseFields(CaseData caseData, HearingBooking hearingBooking) {
        assertThat(caseData.getHearingType()).isEqualTo(hearingBooking.getType());
        assertThat(caseData.getHearingStartDate()).isEqualTo(hearingBooking.getStartDate());
        assertThat(caseData.getHearingEndDate()).isEqualTo(hearingBooking.getEndDate());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(hearingBooking.getJudgeAndLegalAdvisor());
        assertThat(caseData.getPreviousHearingVenue()).isEqualTo(hearingBooking.getPreviousHearingVenue());
    }

    private static HearingBooking hearing(LocalDateTime startDate, String venue) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venueCustomAddress(Address.builder().build())
            .venue(venue)
            .build();
    }

    @SafeVarargs
    private Object dynamicList(UUID selectedId, Element<HearingBooking>... hearings) {
        DynamicList dynamicList = asDynamicList(Arrays.asList(hearings), selectedId, HearingBooking::toLabel);
        return mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {
        });
    }
}
