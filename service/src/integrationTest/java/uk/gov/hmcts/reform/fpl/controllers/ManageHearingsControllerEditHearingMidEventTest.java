package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerEditHearingMidEventTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    ManageHearingsControllerEditHearingMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldPopulatePreviousVenueFieldsWhenUserSelectsAddNewHearing() {
        List<Element<HearingBooking>> hearings = hearings();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingOption", "NEW_HEARING",
                "hearingDetails", hearings))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "edit-hearing");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getPreviousHearingVenue()).isEqualTo(PreviousHearingVenue.builder()
            .previousVenue("Aberdeen Tribunal Hearing Centre, 48 Huntly Street, AB1, Aberdeen, AB10 1SH")
            .build());
    }

    @Test
    void shouldBuildHearingDateListAndPopulateHearingCaseFieldsWhenUserSelectsEditHearing() {
        List<Element<HearingBooking>> hearings = hearings();
        DynamicListElement selectedHearing = DynamicListElement.builder()
            .code(hearings.get(3).getId())
            .label("Case management hearing, 15 March 2099")
            .build();

        DynamicList dynamicList = dynamicList(hearings, selectedHearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingOption", "EDIT_HEARING",
                "hearingDateList", dynamicList,
                "hearingDetails", hearings))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "edit-hearing");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getHearingDateList()).isEqualTo(
            mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {}));

        assertThat(caseData.getFirstHearingFlag()).isNull();
        assertHearingCaseFields(caseData, hearings.get(3).getValue());
    }

    @Test
    void shouldSetFirstHearingFlagWhenUserEditsFirstHearing() {
        List<Element<HearingBooking>> hearings = hearings();
        DynamicListElement selectedHearing = DynamicListElement.builder()
            .code(hearings.get(0).getId())
            .label("Case management hearing, 25 June 2099")
            .build();

        DynamicList dynamicList = dynamicList(hearings, selectedHearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingOption", "EDIT_HEARING",
                "hearingDateList", dynamicList,
                "hearingDetails", hearings))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "edit-hearing");
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getHearingDateList()).isEqualTo(
            mapper.convertValue(dynamicList, new TypeReference<Map<String, Object>>() {}));

        assertThat(caseData.getFirstHearingFlag()).isEqualTo("Yes");
        assertHearingCaseFields(caseData, hearings.get(0).getValue());

    }

    private void assertHearingCaseFields(CaseData caseData, HearingBooking hearingBooking) {
        assertThat(caseData.getHearingType()).isEqualTo(hearingBooking.getType());
        assertThat(caseData.getHearingStartDate()).isEqualTo(hearingBooking.getStartDate());
        assertThat(caseData.getHearingEndDate()).isEqualTo(hearingBooking.getEndDate());
        assertThat(caseData.getJudgeAndLegalAdvisor()).isEqualTo(hearingBooking.getJudgeAndLegalAdvisor());
        assertThat(caseData.getPreviousHearingVenue()).isEqualTo(hearingBooking.getPreviousHearingVenue());
    }

    private List<Element<HearingBooking>> hearings() {
        return List.of(
            element(hearing(LocalDateTime.of(2099, 6, 25, 20, 20), "162")),
            element(hearing(LocalDateTime.of(2020, 2, 2, 20, 20), "96")),
            element(hearing(LocalDateTime.of(2020, 1, 1, 10, 10), "298")),
            element(hearing(LocalDateTime.of(2099, 3, 15, 20, 20), "166"))
        );
    }

    private HearingBooking hearing(LocalDateTime startDate, String venue) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusDays(1))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .venueCustomAddress(Address.builder().build())
            .previousHearingVenue(PreviousHearingVenue.builder().build())
            .venue(venue)
            .build();
    }

    private DynamicList dynamicList(List<Element<HearingBooking>> hearings, DynamicListElement selected) {
        return DynamicList.builder()
            .value(selected)
            .listItems(List.of(
                DynamicListElement.builder()
                    .code(hearings.get(0).getId())
                    .label("Case management hearing, 25 June 2099")
                    .build(),
                DynamicListElement.builder()
                    .code(hearings.get(3).getId())
                    .label("Case management hearing, 15 March 2099")
                    .build()
            ))
            .build();
    }
}
