package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerSubmittedTest extends AbstractControllerTest {

    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    HearingBookingDetailsControllerSubmittedTest() {
        super("add-hearing-bookings");
    }

    @Test
    void shouldTriggerPopulateDatesEventWhenThereAreEmptyDates() {
        postSubmittedEvent(callbackRequestWithEmptyDates());

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            12345L,
            "populateSDO",
            getExpectedData());
    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenThereAreNoEmptyDates() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of("state", "Submitted"))
                .build())
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenCaseIsNotInGatekeepingState() {
        postSubmittedEvent(callbackRequestWithNoEmptyDates());

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    private CallbackRequest callbackRequestWithEmptyDates() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of("state", "Gatekeeping",
                    "hearingDetails", wrapElements(Map.of("startDate", "2050-05-20T13:00")),
                    ALL_PARTIES.getValue(),
                    wrapElements(
                        buildDirection("allParties1"),
                        buildDirection("allParties2", LocalDateTime.of(2060, 1, 1, 13, 0, 0)),
                        buildDirection("allParties3"),
                        buildDirection("allParties4"),
                        buildDirection("allParties5", LocalDateTime.of(2060, 2, 2, 14, 0, 0))),
                    LOCAL_AUTHORITY.getValue(),
                    wrapElements(
                        buildDirection("la1", LocalDateTime.of(2060, 3, 3, 13, 0, 0)),
                        buildDirection("la2", LocalDateTime.of(2060, 4, 4, 14, 0, 0)),
                        buildDirection("la3"),
                        buildDirection("la4"),
                        buildDirection("la5", LocalDateTime.of(2060, 5, 5, 15, 0, 0)),
                        buildDirection("la6"),
                        buildDirection("la7", LocalDateTime.of(2060, 6, 6, 16, 0, 0))),
                    PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                        buildDirection("p&r1")),
                    CAFCASS.getValue(), wrapElements(
                        buildDirection("cafcass1"),
                        buildDirection("cafcass2", LocalDateTime.of(2060, 7, 7, 17, 0, 0)),
                        buildDirection("cafcass3")),
                    OTHERS.getValue(), wrapElements(
                        buildDirection("others1")),
                    COURT.getValue(), wrapElements(
                        buildDirection("court1", LocalDateTime.of(2060, 8, 8, 18, 0, 0)))))
                .build())
            .build();
    }

    private CallbackRequest callbackRequestWithNoEmptyDates() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .data(Map.of(
                    "hearingDetails", wrapElements(Map.of("startDate", "2050-05-20T13:00")),
                    ALL_PARTIES.getValue(),
                    wrapElements(
                        buildDirection("allParties1", LocalDateTime.now()),
                        buildDirection("allParties2", LocalDateTime.now()),
                        buildDirection("allParties3", LocalDateTime.now()),
                        buildDirection("allParties4", LocalDateTime.now()),
                        buildDirection("allParties5", LocalDateTime.now())),
                    LOCAL_AUTHORITY.getValue(),
                    wrapElements(
                        buildDirection("la1", LocalDateTime.now()),
                        buildDirection("la2", LocalDateTime.now()),
                        buildDirection("la3", LocalDateTime.now()),
                        buildDirection("la4", LocalDateTime.now()),
                        buildDirection("la5", LocalDateTime.now()),
                        buildDirection("la6", LocalDateTime.now()),
                        buildDirection("la7", LocalDateTime.now())),
                    PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                        buildDirection("p&r1", LocalDateTime.now())),
                    CAFCASS.getValue(), wrapElements(
                        buildDirection("cafcass1", LocalDateTime.now()),
                        buildDirection("cafcass2", LocalDateTime.now()),
                        buildDirection("cafcass3", LocalDateTime.now())),
                    OTHERS.getValue(), wrapElements(
                        buildDirection("others1", LocalDateTime.now())),
                    COURT.getValue(), wrapElements(
                        buildDirection("court1", LocalDateTime.now()))))
                .build())
            .build();
    }

    private Map<String, Object> getExpectedData() {
        Map<String, Object> hearingDetails = new HashMap<>();
        hearingDetails.put("id", null);
        hearingDetails.put("value", Map.of("startDate", "2050-05-20T13:00"));

        return Map.of("state", "Gatekeeping",
            "hearingDetails", List.of(hearingDetails),
            ALL_PARTIES.getValue(),
            wrapElements(
                buildDirection("allParties1", LocalDateTime.of(2050, 5, 17, 12, 0, 0)),
                buildDirection("allParties2", LocalDateTime.of(2060, 1, 1, 13, 0, 0)),
                buildDirection("allParties3", LocalDateTime.of(2050, 5, 18, 16, 0, 0)),
                buildDirection("allParties4", LocalDateTime.of(2050, 5, 20, 0, 0, 0)),
                buildDirection("allParties5", LocalDateTime.of(2060, 2, 2, 14, 0, 0))),
            LOCAL_AUTHORITY.getValue(),
            wrapElements(
                buildDirection("la1", LocalDateTime.of(2060, 3, 3, 13, 0, 0)),
                buildDirection("la2", LocalDateTime.of(2060, 4, 4, 14, 0, 0)),
                buildDirection("la3", LocalDateTime.of(2050, 5, 20, 0, 0, 0)),
                buildDirection("la4", LocalDateTime.of(2050, 5, 18, 16, 0, 0)),
                buildDirection("la5", LocalDateTime.of(2060, 5, 5, 15, 0, 0)),
                buildDirection("la6", LocalDateTime.of(2050, 5, 19, 12, 0, 0)),
                buildDirection("la7", LocalDateTime.of(2060, 6, 6, 16, 0, 0))),
            PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                buildDirection("p&r1", LocalDateTime.of(2050, 5, 17, 12, 0, 0))),
            CAFCASS.getValue(), wrapElements(
                buildDirection("cafcass1", LocalDateTime.of(2050, 5, 18, 0, 0, 0)),
                buildDirection("cafcass2", LocalDateTime.of(2060, 7, 7, 17, 0, 0)),
                buildDirection("cafcass3", LocalDateTime.of(2050, 5, 18, 16, 0, 0))),
            OTHERS.getValue(), wrapElements(
                buildDirection("others1", LocalDateTime.of(2050, 5, 18, 16, 0, 0))),
            COURT.getValue(), wrapElements(
                buildDirection("court1", LocalDateTime.of(2060, 8, 8, 18, 0, 0))));
    }

    private Direction buildDirection(String text) {
        return Direction.builder().directionText(text).responses(List.of()).build();
    }

    private Direction buildDirection(String text, LocalDateTime dateTime) {
        return Direction.builder().directionText(text).responses(List.of()).dateToBeCompletedBy(dateTime).build();
    }
}
