package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.calendar.model.BankHolidays.Division;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
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
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class ManageHearingsControllerSubmittedTest extends ManageHearingsControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_NUMBER = "FMN1";
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String JUDGE_EMAIL = "judge@judge.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document NOTICE_OF_HEARING_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final byte[] NOTICE_OF_HEARING_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinary();

    private final Element<HearingBooking> hearingWithoutNotice = element(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
        .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
        .noticeOfHearing(null)
        .build());

    private static final Element<Representative> REPRESENTATIVE_POST = element(Representative.builder()
        .fullName("First Representative")
        .servingPreferences(POST)
        .address(testAddress())
        .build());

    private static final Element<Representative> REPRESENTATIVE_EMAIL = element(Representative.builder()
        .fullName("Third Representative")
        .servingPreferences(EMAIL)
        .email("third@representatives.com")
        .build());

    private static final Element<Representative> REPRESENTATIVE_DIGITAL = element(Representative.builder()
        .fullName("Second Representative")
        .servingPreferences(DIGITAL_SERVICE)
        .email("second@representatives.com")
        .build());

    private static final Respondent RESPONDENT_NOT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Alex")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .build();

    private static final Respondent RESPONDENT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("George")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .representedBy(wrapElements(REPRESENTATIVE_POST.getId(), REPRESENTATIVE_DIGITAL.getId()))
        .build();

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseCaptor;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequestCaptor;

    @MockBean
    private BankHolidaysApi bankHolidaysApi;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private OtherRecipientsInbox otherRecipientsInbox;

    ManageHearingsControllerSubmittedTest() {
        super("manage-hearings");
    }

    @Test
    void shouldTriggerPopulateDatesEventWhenEmptyDatesExistAndCaseInGatekeeping() {
        given(bankHolidaysApi.retrieveAll()) // there are no holidays :(
            .willReturn(BankHolidays.builder().englandAndWales(Division.builder().events(List.of()).build()).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Gatekeeping")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq("populateSDO"),
            anyMap());

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary("Yes", "Case management", LocalDate.of(2050, 5, 20)));

        verifyNoMoreInteractions(coreCaseDataService);

    }

    @Test
    void shouldNotTriggerPopulateDatesEventWhenCaseNotInGatekeeping() {
        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Submitted")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldDoNothingWhenNoHearingAddedOrUpdated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary("Yes", "Case management", LocalDate.of(2050, 5, 20)));
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerSendNoticeOfHearingEventForNewHearingWhenNoticeOfHearingPresent()
        throws NotificationClientException {
        final DocumentReference noticeOfHearing = testDocumentReference();

        final Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .noticeOfHearing(noticeOfHearing)
            .venue("96")
            .build());

        final Element<HearingBooking> existingHearing = element(HearingBooking.builder()
            .type(ISSUE_RESOLUTION)
            .startDate(LocalDateTime.of(2020, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2020, 5, 20, 14, 0))
            .noticeOfHearing(testDocumentReference())
            .venue("162")
            .others(emptyList())
            .build());

        final CaseData cdb = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .hearingDetails(List.of(existingHearing))
            .representatives(List.of(REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL, REPRESENTATIVE_POST))
            .respondents1(wrapElements(RESPONDENT_REPRESENTED, RESPONDENT_NOT_REPRESENTED))
            .build();

        final CaseData cd = cdb.toBuilder()
            .hearingDetails(List.of(hearingWithNotice, existingHearing))
            .selectedHearingId(hearingWithNotice.getId())
            .build();

        givenFplService();

        given(documentDownloadService.downloadDocument(noticeOfHearing.getBinaryUrl()))
            .willReturn(NOTICE_OF_HEARING_BINARY);

        given(otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL,
            cdb,
            emptyList(),
            element -> element.getValue().getEmail())
        ).willReturn(emptySet());

        given(sendLetterApi.sendLetter(eq(SERVICE_AUTH_TOKEN), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_1_ID))
            .willReturn(new SendLetterResponse(LETTER_2_ID));

        given(uploadDocumentService.uploadPDF(NOTICE_OF_HEARING_BINARY, noticeOfHearing.getFilename()))
            .willReturn(NOTICE_OF_HEARING_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, "Coversheet.pdf"))
            .willReturn(COVERSHEET_REPRESENTATIVE);
        given(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, "Coversheet.pdf"))
            .willReturn(COVERSHEET_RESPONDENT);

        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, REPRESENTATIVE_POST.getValue()))
            .willReturn(testDocmosisDocument(COVERSHEET_REPRESENTATIVE_BINARY));
        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, RESPONDENT_NOT_REPRESENTED.getParty()))
            .willReturn(testDocmosisDocument(COVERSHEET_RESPONDENT_BINARY));

        postSubmittedEvent(toCallBackRequest(cd, cdb));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(REPRESENTATIVE_EMAIL.getValue().getEmail()),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2)).sendLetter(
            eq(SERVICE_AUTH_TOKEN),
            printRequestCaptor.capture());

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).updateCase(
            eq(CASE_ID), caseCaptor.capture());

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(CASE_ID, noticeOfHearing,
            COVERSHEET_REPRESENTATIVE_BINARY, NOTICE_OF_HEARING_BINARY);

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(CASE_ID, noticeOfHearing,
            COVERSHEET_RESPONDENT_BINARY, NOTICE_OF_HEARING_BINARY);

        SentDocument expectedDocumentSentToRepresentative = documentSent(REPRESENTATIVE_POST.getValue(),
            COVERSHEET_REPRESENTATIVE, NOTICE_OF_HEARING_DOCUMENT, LETTER_1_ID, now());

        SentDocument expectedDocumentSentToRespondent = documentSent(RESPONDENT_NOT_REPRESENTED.getParty(),
            COVERSHEET_RESPONDENT, NOTICE_OF_HEARING_DOCUMENT, LETTER_2_ID, now());

        assertThat(printRequestCaptor.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest1, expectedPrintRequest2));

        final CaseData caseUpdate = getCase(caseCaptor);

        assertThat(caseUpdate.getDocumentsSentToParties()).hasSize(2);

        assertThat(caseUpdate.getDocumentsSentToParties().get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRepresentative);

        assertThat(caseUpdate.getDocumentsSentToParties().get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRespondent);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
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
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", NEW_HEARING,
                "hearingDetails", List.of(hearingWithNotice),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE),
            eq(JUDGE_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
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
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", hearingOption,
                "hearingReListOption", RE_LIST_NOW,
                "hearingDetails", List.of(hearingWithNotice),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(TEMP_JUDGE_ALLOCATED_TO_HEARING_TEMPLATE),
            eq(JUDGE_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
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
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", NEW_HEARING,
                "hearingDetails", List.of(hearingWithNotice),
                "allocatedJudge", Judge.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Watson")
                    .build(),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
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
            .id(CASE_ID)
            .data(Map.of(
                "selectedHearingId", hearingWithNotice.getId(),
                "hearingOption", hearingOption,
                "hearingDetails", List.of(hearingWithNotice),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE
            ))
            .state("Submitted")
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());

        verifyNoMoreInteractions(coreCaseDataService);
    }

    private Map<String, Object> buildData(List<Element<HearingBooking>> hearings, UUID selectedHearing) {
        return Map.of("hearingDetails", hearings,
            "selectedHearingId", selectedHearing,
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
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
        return caseConverter.toMap(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing(hasNextHearing)
            .caseSummaryNextHearingType(hearingType)
            .caseSummaryNextHearingDate(hearingDate)
            .caseSummaryCourtName(COURT_NAME)
            .caseSummaryLanguageRequirement("No")
            .caseSummaryLALanguageRequirement("No")
            .build());
    }

}
