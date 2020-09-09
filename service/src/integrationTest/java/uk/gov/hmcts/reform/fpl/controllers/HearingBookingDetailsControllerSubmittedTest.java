package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ActiveProfiles("integration-test")
@WebMvcTest(HearingBookingDetailsController.class)
@OverrideAutoConfiguration(enabled = true)
class HearingBookingDetailsControllerSubmittedTest extends AbstractControllerTest {

    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final long CASE_ID = 12345L;
    private static final String CASE_REFERENCE = "12345";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

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
    void shouldNotTriggerPopulateDatesEventWhenCaseIsNotInGatekeepingState() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .state("Submitted")
                .data(Map.of())
                .build())
            .build();

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any()));
    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenThereAreNoEmptyDates() {
        postSubmittedEvent(callbackRequestWithNoEmptyDates());

        checkThat(() -> verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any()));
    }

    @Test
    void shouldInvokeNotificationClientWhenNewHearingsHaveBeenAdded() {
        HearingBooking newBooking = createHearingBooking(now().plusHours(2), now().plusDays(2));
        HearingBooking existingBooking = createHearingBooking(now().plusHours(2), now());

        List<Element<HearingBooking>> hearingBookings = List.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(newBooking)
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("a14ce00f-e151-47f2-8e5f-374cc6fc7562"))
                .value(existingBooking)
                .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingIds", wrapElements(UUID.fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657")),
                "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
                "familyManCaseNumber", "111222",
                "hearingDetails", hearingBookings,
                "representatives", createRepresentatives(RepresentativeServingPreferences.EMAIL),
                "respondents1", createRespondents()
            )).build();

        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        postSubmittedEvent(caseDetails);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                eq(NOTICE_OF_NEW_HEARING),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                anyMap(),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                eq(NOTICE_OF_NEW_HEARING),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                eq(NOTICE_OF_NEW_HEARING),
                eq("abc@example.com"),
                anyMap(),
                eq(NOTIFICATION_REFERENCE));
        });
    }

    @Test
    void shouldNotInvokeNotificationClientWhenNoNewHearingsArePresent() {
        postSubmittedEvent(callbackRequestWithNoEmptyDates());

        checkThat(() -> verify(notificationClient, never()).sendEmail(any(), any(), any(), any(), any()));
    }

    private CallbackRequest callbackRequestWithEmptyDates() {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .state("Gatekeeping")
                .data(Map.of("hearingDetails", wrapElements(Map.of("startDate", "2050-05-20T13:00")),
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
                .id(CASE_ID)
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .state("Submitted")
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

        return Map.of("hearingDetails", List.of(hearingDetails),
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
        return Direction.builder().directionText(text).build();
    }

    private Direction buildDirection(String text, LocalDateTime dateTime) {
        return Direction.builder().directionText(text).dateToBeCompletedBy(dateTime).build();
    }
}
