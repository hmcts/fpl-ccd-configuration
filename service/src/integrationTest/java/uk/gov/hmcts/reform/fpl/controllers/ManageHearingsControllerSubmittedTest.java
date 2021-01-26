package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.calendar.model.BankHolidays.Division;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Long.parseLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_NOW;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class ManageHearingsControllerSubmittedTest extends ManageHearingsControllerTest {

    private static final String CASE_ID = "12345";
    private static final long CASE_REFERENCE = 12345L;
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "local-authority@local-authority.com";
    private static final String JUDGE_EMAIL = "judge@judge.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + parseLong(CASE_ID);

    private final Element<HearingBooking> hearingWithoutNotice = element(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
        .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
        .noticeOfHearing(null)
        .build());

    @MockBean
    private BankHolidaysApi bankHolidaysApi;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @SpyBean
    private EventService eventPublisher;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ObjectMapper objectMapper;

    ManageHearingsControllerSubmittedTest() {
        super("manage-hearings");
    }

    @BeforeEach
    void setUp() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(true);
    }

    @Test
    void shouldTriggerPopulateDatesEventWhenEmptyDatesExistAndCaseInGatekeeping() {
        given(bankHolidaysApi.retrieveAll()) // there are no holidays :(
            .willReturn(BankHolidays.builder().englandAndWales(Division.builder().events(List.of()).build()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Gatekeeping")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_REFERENCE),
            eq("populateSDO"),
            anyMap());

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_REFERENCE,
            "internal-update-case-summary",
            caseSummary("Yes", "Case management", LocalDate.of(2050, 5, 20)));

        verifyNoMoreInteractions(coreCaseDataService);

    }

    @Test
    void shouldTriggerPopulateDatesEventWhenEmptyDatesExistAndCaseInGatekeepingToggledOff() {
        when(featureToggleService.isSummaryTabOnEventEnabled()).thenReturn(false);

        given(bankHolidaysApi.retrieveAll()) // there are no holidays :(
            .willReturn(BankHolidays.builder().englandAndWales(Division.builder().events(List.of()).build()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Gatekeeping")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_REFERENCE),
            eq("populateSDO"),
            anyMap());

        verifyNoInteractions(notificationClient, coreCaseDataService);
    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenCaseNotInGatekeeping() {
        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Submitted")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldDoNothingWhenNoHearingAddedOrUpdated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_REFERENCE,
            "internal-update-case-summary",
            caseSummary("Yes", "Case management", LocalDate.of(2050, 5, 20)));
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerSendNoticeOfHearingEventForNewHearingWhenNoticeOfHearingPresent()
        throws NotificationClientException {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .noticeOfHearing(testDocumentReference())
            .venue("96")
            .build());

        Element<HearingBooking> existingHearing = element(HearingBooking.builder()
            .type(ISSUE_RESOLUTION)
            .startDate(LocalDateTime.of(2020, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2020, 5, 20, 14, 0))
            .noticeOfHearing(testDocumentReference())
            .venue("162")
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(buildData(List.of(hearingWithNotice, existingHearing), hearingWithNotice.getId()))
            .state("Submitted")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(buildData(List.of(existingHearing), hearingWithoutNotice.getId()))
            .build();

        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

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

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_REFERENCE,
            "internal-change-SEND_DOCUMENT",
            Map.of("documentToBeSent", hearingWithNotice.getValue().getNoticeOfHearing()));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerTemporaryHearingJudgeEventWhenCreatingANewHearingWithTemporaryJudgeAllocated()
        throws NotificationClientException {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .judgeLastName("Davidson")
                .judgeTitle(HER_HONOUR_JUDGE)
                .build())
            .hearingJudgeLabel("Her Honour Judge Davidson")
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", NEW_HEARING,
                "hearingDetails", List.of(hearingWithNotice)
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE),
            eq(JUDGE_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"VACATE_HEARING", "ADJOURN_HEARING"})
    void shouldTriggerTemporaryHearingJudgeEventWhenReListingAHearing(HearingOptions hearingOption)
        throws NotificationClientException {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .judgeLastName("Davidson")
                .judgeTitle(HER_HONOUR_JUDGE)
                .build())
            .hearingJudgeLabel("Her Honour Judge Davidson")
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", hearingOption,
                "hearingReListOption", RE_LIST_NOW,
                "hearingDetails", List.of(hearingWithNotice)
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE),
            eq(JUDGE_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldNotTriggerTemporaryHearingJudgeEventWhenUsingAllocatedJudge() {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", NEW_HEARING,
                "hearingDetails", List.of(hearingWithNotice),
                "allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Watson")
                    .build()
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"EDIT_HEARING", "ADJOURN_HEARING", "VACATE_HEARING"})
    void shouldNotTriggerTemporaryHearingJudgeEventWhenAdjourningOrVacatingAHearingWithoutReListing(
        HearingOptions hearingOption) {
        Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .judgeLastName("Davidson")
                .judgeTitle(HER_HONOUR_JUDGE)
                .build())
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(parseLong(CASE_ID))
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", hearingOption,
                "hearingDetails", List.of(hearingWithNotice)
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_REFERENCE),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    private Map<String, Object> buildData(List<Element<HearingBooking>> hearings, UUID selectedHearing) {
        return Map.of("hearingDetails", hearings,
            "selectedHearingId", selectedHearing,
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

    private Map<String, Object> caseSummary(String hasNextHearing, String hearingType, LocalDate hearingDate) {
        return objectMapper.convertValue(
            SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing(hasNextHearing)
                .caseSummaryNextHearingType(hearingType)
                .caseSummaryNextHearingDate(hearingDate)
                .build(), new TypeReference<>() {});
    }
}
