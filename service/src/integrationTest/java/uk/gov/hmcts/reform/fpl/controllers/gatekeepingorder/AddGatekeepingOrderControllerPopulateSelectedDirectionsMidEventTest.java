package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ATTEND_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.bankHolidays;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerPopulateSelectedDirectionsMidEventTest extends AbstractCallbackTest {

    AddGatekeepingOrderControllerPopulateSelectedDirectionsMidEventTest() {
        super("add-gatekeeping-order");
    }

    @MockBean
    private BankHolidaysApi bankHolidaysApi;

    @Test
    void shouldPrepareSelectedStandardDirectionsWhenHearingNotPresent() {

        List<DirectionType> selectedDirectionsForAll = List.of(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE, ATTEND_HEARING);
        List<DirectionType> selectedDirectionsForCafcass = List.of(APPOINT_CHILDREN_GUARDIAN);

        CaseData caseData = CaseData.builder()
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .directionsForAllParties(selectedDirectionsForAll)
                .directionsForCafcass(selectedDirectionsForCafcass)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "direction-selection");

        assertThat(getStandardDirection(callbackResponse, REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)).isEqualTo(
            StandardDirection.builder()
                .type(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)
                .title("Request permission for expert evidence")
                .description("Your request must be in line with Family Procedure Rules part 25 and "
                    + "Practice Direction 25C. Give other parties a list of names of suitable experts.")
                .assignee(ALL_PARTIES)
                .dueDateType(DAYS)
                .daysBeforeHearing(3)
                .build());

        assertThat(getStandardDirection(callbackResponse, ATTEND_HEARING)).isEqualTo(
            StandardDirection.builder()
                .type(ATTEND_HEARING)
                .title("Attend the pre-hearing and hearing")
                .description("Parties and their legal representatives must attend the pre-hearing and hearing")
                .assignee(ALL_PARTIES)
                .dueDateType(DAYS)
                .daysBeforeHearing(0)
                .build());

        assertThat(getStandardDirection(callbackResponse, APPOINT_CHILDREN_GUARDIAN)).isEqualTo(
            StandardDirection.builder()
                .type(APPOINT_CHILDREN_GUARDIAN)
                .title("Appoint a children's guardian")
                .description("")
                .assignee(CAFCASS)
                .dueDateType(DAYS)
                .daysBeforeHearing(2)
                .build());
    }

    @Test
    void shouldPrepareSelectedStandardDirectionWhenHearingPresent() {

        HearingBooking firstHearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2030, 2, 10, 15, 0, 0))
            .build();

        HearingBooking secondHearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2030, 5, 10, 15, 0, 0))
            .build();

        List<DirectionType> selectedDirectionsForAll = List.of(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE, ATTEND_HEARING);
        List<DirectionType> selectedDirectionsForCafcass = List.of(APPOINT_CHILDREN_GUARDIAN);

        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(firstHearing, secondHearing))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .directionsForAllParties(selectedDirectionsForAll)
                .directionsForCafcass(selectedDirectionsForCafcass)
                .build())
            .build();

        given(bankHolidaysApi.retrieveAll()).willReturn(bankHolidays(LocalDate.of(2030, 2, 9)));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "direction-selection");

        assertThat(getStandardDirection(response, REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)).isEqualTo(
            StandardDirection.builder()
                .type(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE)
                .title("Request permission for expert evidence")
                .description("Your request must be in line with Family Procedure Rules part 25 "
                    + "and Practice Direction 25C. Give other parties a list of names of suitable experts.")
                .assignee(ALL_PARTIES)
                .dueDateType(DAYS)
                .daysBeforeHearing(3)
                .dateToBeCompletedBy(LocalDateTime.of(2030, 2, 6, 12, 0, 0))
                .build());

        assertThat(getStandardDirection(response, ATTEND_HEARING)).isEqualTo(
            StandardDirection.builder()
                .type(ATTEND_HEARING)
                .title("Attend the pre-hearing and hearing")
                .description("Parties and their legal representatives must attend the pre-hearing and hearing")
                .assignee(ALL_PARTIES)
                .dueDateType(DAYS)
                .daysBeforeHearing(0)
                .dateToBeCompletedBy(LocalDateTime.of(2030, 2, 10, 0, 0, 0))
                .build());

        assertThat(getStandardDirection(response, APPOINT_CHILDREN_GUARDIAN)).isEqualTo(
            StandardDirection.builder()
                .type(APPOINT_CHILDREN_GUARDIAN)
                .title("Appoint a children's guardian")
                .description("")
                .assignee(CAFCASS)
                .dueDateType(DAYS)
                .daysBeforeHearing(2)
                .dateToBeCompletedBy(LocalDateTime.of(2030, 2, 7, 16, 0, 0))
                .build());
    }

    private StandardDirection getStandardDirection(AboutToStartOrSubmitCallbackResponse response, DirectionType type) {
        return caseConverter.convert(response.getData().get("direction-" + type), StandardDirection.class);
    }
}
