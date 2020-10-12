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
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageHearingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageHearingsControllerSubmittedTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + parseLong(CASE_ID);

    private List<Element<HearingBooking>> hearingWithoutNotice = wrapElements(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2050, 5, 20, 13, 00))
        .noticeOfHearing(null)
        .build());

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    ManageHearingsControllerSubmittedTest() {
        super("manage-hearings");
    }

    @Test
    void shouldTriggerPopulateDatesEventWhenEmptyDatesExistAndCaseInGatekeeping() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .id(parseLong(CASE_ID))
                .data(buildData(hearingWithoutNotice))
                .state("Gatekeeping")
                .build())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of()).build())
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(12345L),
            eq("populateSDO"),
            anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenCaseNotInGatekeeping() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .id(parseLong(CASE_ID))
                .data(buildData(hearingWithoutNotice))
                .state("Submitted")
                .build())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of()).build())
            .build();

        postSubmittedEvent(callbackRequest);

        verifyNoInteractions(coreCaseDataService);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldTriggerNewHearingsAddedEventForNewHearingWhenNoticeOfHearingPresent()
        throws NotificationClientException {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 00))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 00))
            .noticeOfHearing(testDocumentReference())
            .venue("96")
            .build());

        Element<HearingBooking> existingHearing = element(HearingBooking.builder()
            .type(ISSUE_RESOLUTION)
            .startDate(LocalDateTime.of(2020, 5, 20, 13, 00))
            .endDate(LocalDateTime.of(2020, 5, 20, 14, 00))
            .noticeOfHearing(testDocumentReference())
            .venue("162")
            .build());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .id(parseLong(CASE_ID))
                .data(buildData(List.of(hearingWithNotice, existingHearing)))
                .state("Submitted")
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .data(buildData(List.of(existingHearing))).build())
            .build();

        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        postSubmittedEvent(callbackRequest);

        verifyNoInteractions(coreCaseDataService);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq("abc@example.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));
    }

    private Map<String, Object> buildData(List<Element<HearingBooking>> hearings) {
        return Map.of("hearingDetails", hearings,
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "representatives", createRepresentatives(RepresentativeServingPreferences.EMAIL),
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
                buildDirection("court1", LocalDateTime.of(2060, 8, 8, 18, 0, 0))));
    }

    private Direction buildDirection(String text) {
        return Direction.builder().directionText(text).build();
    }

    private Direction buildDirection(String text, LocalDateTime dateTime) {
        return Direction.builder().directionText(text).dateToBeCompletedBy(dateTime).build();
    }
}
