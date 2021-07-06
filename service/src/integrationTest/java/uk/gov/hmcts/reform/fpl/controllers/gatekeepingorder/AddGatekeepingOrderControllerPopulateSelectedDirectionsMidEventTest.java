package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ATTEND_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.bankHolidays;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerPopulateSelectedDirectionsMidEventTest extends AbstractCallbackTest {

    private static final String CALLBACK_NAME = "direction-selection";

    @MockBean
    private BankHolidaysApi bankHolidaysApi;

    AddGatekeepingOrderControllerPopulateSelectedDirectionsMidEventTest() {
        super("add-gatekeeping-order");
    }

    @Test
    void shouldPrepareSelectedStandardDirectionsWhenHearingNotPresent() {

        List<DirectionType> selectedDirectionsForAll = List.of(REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE, ATTEND_HEARING);
        List<DirectionType> selectedDirectionsForCafcass = List.of(APPOINT_CHILDREN_GUARDIAN);

        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .directionsForAllParties(selectedDirectionsForAll)
                .directionsForCafcass(selectedDirectionsForCafcass)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, CALLBACK_NAME);

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
            .gatekeepingOrderRouter(SERVICE)
            .hearingDetails(wrapElements(firstHearing, secondHearing))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .directionsForAllParties(selectedDirectionsForAll)
                .directionsForCafcass(selectedDirectionsForCafcass)
                .build())
            .build();

        given(bankHolidaysApi.retrieveAll()).willReturn(bankHolidays(LocalDate.of(2030, 2, 9)));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, CALLBACK_NAME);

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

    @Test
    void shouldSetHearingDateIfHearingPresent() {
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .hearingDetails(wrapElements(HearingBooking.builder()
                .type(HearingType.CASE_MANAGEMENT)
                .startDate(LocalDateTime.of(2030, 2, 10, 10, 30, 0))
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, CALLBACK_NAME);

        assertThat(callbackResponse.getData()).contains(
            entry("gatekeepingOrderHasHearing1", "YES"),
            entry("gatekeepingOrderHasHearing2", "YES"),
            entry("gatekeepingOrderHearingDate1", "10 February 2030, 10:30am"),
            entry("gatekeepingOrderHearingDate2", "10 February 2030, 10:30am")
        );
    }

    @Test
    void shouldNotSetHearingDateIfHearingNotPresent() {
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(SERVICE)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, CALLBACK_NAME);

        assertThat(callbackResponse.getData()).doesNotContainKeys(
            "gatekeepingOrderHasHearing1",
            "gatekeepingOrderHasHearing2",
            "gatekeepingOrderHearingDate1",
            "gatekeepingOrderHearingDate2");
    }

    @ParameterizedTest
    @EnumSource(value = GatekeepingOrderRoute.class, mode = EnumSource.Mode.EXCLUDE, names = "SERVICE")
    void shouldThrowExceptionWhenInvokedForInvalidRoute(GatekeepingOrderRoute route) {
        CaseData caseData = CaseData.builder()
            .gatekeepingOrderRouter(route)
            .build();

        assertThatThrownBy(() -> postMidEvent(caseData, CALLBACK_NAME))
            .hasMessageContaining(String.format("The %s callback does not support %s route", CALLBACK_NAME, route));
    }

    private StandardDirection getStandardDirection(AboutToStartOrSubmitCallbackResponse response, DirectionType type) {
        return caseConverter.convert(response.getData().get("direction-" + type), StandardDirection.class);
    }
}
