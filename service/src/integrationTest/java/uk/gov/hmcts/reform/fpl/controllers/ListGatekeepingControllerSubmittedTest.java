package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
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
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ListGatekeepingControllerSubmittedTest extends ManageHearingsControllerTest {

    private static final long CASE_ID = 12345L;
    private static final String FAMILY_MAN_NUMBER = "FMN1";
    private static final String JUDGE_EMAIL = "judge@judge.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document NOTICE_OF_HEARING_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final byte[] NOTICE_OF_HEARING_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinary();
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
    private static final Child CHILDREN = Child.builder()
        .party(ChildParty.builder()
            .firstName("Jade")
            .lastName("Connor")
            .dateOfBirth(LocalDate.now())
            .address(testAddress())
            .build())
        .build();
    private final Element<HearingBooking> hearingWithoutNotice = element(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
        .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
        .noticeOfHearing(null)
        .build());
    @Captor
    private ArgumentCaptor<Map<String, Object>> caseCaptor;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequestCaptor;

    @Captor
    private ArgumentCaptor<NoticeOfHearingCafcassData> noticeOfHearingCafcassDataCaptor;

    @Captor
    private ArgumentCaptor<StartEventResponse> startEventResponseArgumentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> eventDataCaptor;

    @Captor
    private ArgumentCaptor<String> eventIdCaptor;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private OtherRecipientsInbox otherRecipientsInbox;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    @MockBean
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    ListGatekeepingControllerSubmittedTest() {
        super("list-gatekeeping-hearing");
    }

    @Test
    @Order(2)
    void shouldNotTriggerPopulateDatesEventWhenCaseNotInGatekeeping() {
        CaseDetails caseDetails = CaseDetails.builder()
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .state("Submitted")
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder().data(Map.of()).build();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verifyNoInteractions(notificationClient);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(eq(CASE_ID), eq("internal-update-case-summary"));
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .submitEvent(startEventResponseArgumentCaptor.capture(), eq(CASE_ID), eventDataCaptor.capture());
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(eq(CASE_ID), eq("internal-change-add-gatekeeping"));

        assertThat(startEventResponseArgumentCaptor.getAllValues().stream().map(StartEventResponse::getEventId))
            .containsExactly("internal-update-case-summary");

        verifyNoMoreInteractions(concurrencyHelper);
    }

    @Test
    @Order(1)
    void shouldDoNothingWhenNoHearingAddedOrUpdated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .build();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDetails);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT)).startEvent(CASE_ID,
            "internal-update-case-summary");
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT)).submitEvent(any(),
            eq(CASE_ID),
            eq(caseSummary("Yes", "Case management",
                LocalDate.of(2050, 5, 20))));
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(CASE_ID, "internal-change-add-gatekeeping");
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT)).submitEvent(any(), eq(CASE_ID), anyMap());

        verifyNoInteractions(notificationClient);
        verifyNoMoreInteractions(concurrencyHelper);
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
            .caseLocalAuthority(LOCAL_AUTHORITY_3_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_3_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_3_INBOX)
                .build()))
            .hearingDetails(List.of(existingHearing))
            .representatives(List.of(REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL, REPRESENTATIVE_POST))
            .respondents1(wrapElements(RESPONDENT_REPRESENTED, RESPONDENT_NOT_REPRESENTED))
            .build();

        final CaseData cd = cdb.toBuilder()
            .hearingDetails(List.of(hearingWithNotice, existingHearing))
            .selectedHearingId(hearingWithNotice.getId())
            .gatekeepingOrderRouter(SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .build())
            .build();

        givenFplService();

        given(documentDownloadService.downloadDocument(noticeOfHearing.getBinaryUrl()))
            .willReturn(NOTICE_OF_HEARING_BINARY);

        given(documentConversionService.convertToPdf(noticeOfHearing))
            .willReturn(noticeOfHearing);
        given(documentConversionService.convertToPdfBytes(any()))
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

        given(uploadDocumentService.uploadPDF(eq(NOTICE_OF_HEARING_BINARY), any()))
            .willReturn(NOTICE_OF_HEARING_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_REPRESENTATIVE);
        given(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_RESPONDENT);

        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, REPRESENTATIVE_POST.getValue(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_REPRESENTATIVE_BINARY));
        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, RESPONDENT_NOT_REPRESENTED.getParty(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_RESPONDENT_BINARY));

        final StartEventResponse updateCaseResp = StartEventResponse.builder()
            .caseDetails(asCaseDetails(cd))
            .eventId("internal-update-case-summary")
            .token("token")
            .build();

        final StartEventResponse gatekeepingCaseResp = StartEventResponse.builder()
            .caseDetails(asCaseDetails(cd))
            .eventId("internal-update-add-gatekeeping")
            .token("token")
            .build();

        when(concurrencyHelper.startEvent(any(), eq("internal-update-case-summary")))
            .thenReturn(updateCaseResp);

        when(concurrencyHelper.startEvent(any(), eq("internal-change-add-gatekeeping")))
            .thenReturn(gatekeepingCaseResp);

        postSubmittedEvent(toCallBackRequest(cd, cdb));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(LOCAL_AUTHORITY_3_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT)).submitEvent(eq(updateCaseResp),
            eq(CASE_ID), caseCaptor.capture());

        final CaseData caseUpdate = getCase(caseCaptor);

        assertThat(caseUpdate.getDocumentsSentToParties()).isNullOrEmpty();

        verify(concurrencyHelper, times(2)).startEvent(eq(CASE_ID), any());
        // once for gatekeeping, once for posting, NOT for sealing
        verify(concurrencyHelper, times(1)).submitEvent(any(), eq(CASE_ID), anyMap());

        verify(cafcassNotificationService, never()).sendEmail(any(), any(), any(), any());

        verifyNoMoreInteractions(concurrencyHelper);
        verifyNoMoreInteractions(notificationClient);
        verifyNoMoreInteractions(sendLetterApi);
    }

    @Test
    void shouldTriggerSendNoticeOfHearingEventForNewHearingWhenNoticeOfHearingPresentEnglandLa()
        throws NotificationClientException {
        final DocumentReference noticeOfHearing = testDocumentReference();

        LocalDateTime startDate = LocalDateTime.of(2050, 5, 20, 13, 0);

        final Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
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
            .children1(wrapElements(CHILDREN))
            .build();

        final CaseData cd = cdb.toBuilder()
            .hearingDetails(List.of(hearingWithNotice, existingHearing))
            .selectedHearingId(hearingWithNotice.getId())
            .build();

        givenFplService();

        given(uploadDocumentService.uploadPDF(eq(NOTICE_OF_HEARING_BINARY), any()))
            .willReturn(NOTICE_OF_HEARING_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_REPRESENTATIVE);
        given(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_RESPONDENT);

        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, REPRESENTATIVE_POST.getValue(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_REPRESENTATIVE_BINARY));
        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, RESPONDENT_NOT_REPRESENTED.getParty(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_RESPONDENT_BINARY));

        given(documentConversionService.convertToPdfBytes(any())).willReturn(NOTICE_OF_HEARING_BINARY);

        given(documentDownloadService.downloadDocument(noticeOfHearing.getBinaryUrl()))
            .willReturn(NOTICE_OF_HEARING_BINARY);

        given(otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL,
            cdb,
            emptyList(),
            element -> element.getValue().getEmail())
        ).willReturn(emptySet());

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(cd))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(toCallBackRequest(cd, cdb));

        verify(notificationClient, never()).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(cafcassNotificationService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            isA(CaseData.class),
            eq(Set.of(noticeOfHearing)),
            same(NOTICE_OF_HEARING),
            noticeOfHearingCafcassDataCaptor.capture()
        );

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2))
            .startEvent(eq(CASE_ID), any());
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(1))
            .submitEvent(any(), eq(CASE_ID), anyMap());

        NoticeOfHearingCafcassData noticeOfHearingCafcassData = noticeOfHearingCafcassDataCaptor.getValue();

        assertThat(noticeOfHearingCafcassData.getFirstRespondentName())
            .isEqualTo("Jones");
        assertThat(noticeOfHearingCafcassData.getEldestChildLastName())
            .isEqualTo("Connor");
        assertThat(noticeOfHearingCafcassData.getHearingType())
            .isEqualTo("case management");
        assertThat(noticeOfHearingCafcassData.getHearingDate())
            .isEqualTo(startDate);
        assertThat(noticeOfHearingCafcassData.getHearingVenue())
            .isEqualTo("Aberdeen Tribunal Hearing Centre, 48 Huntly Street, AB1, Aberdeen, AB10 1SH");
        assertThat(noticeOfHearingCafcassData.getPreHearingTime())
            .isEqualTo("1 hour before the hearing");
        assertThat(noticeOfHearingCafcassData.getHearingTime())
            .isEqualTo("1:00pm - 2:00pm");

        verifyNoMoreInteractions(concurrencyHelper);
    }

    @Test
    void shouldNotTriggerTemporaryHearingJudgeEventWhenCreatingANewHearingWithTemporaryJudgeAllocated()
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

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDetails);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(eq(CASE_ID), eq("internal-update-case-summary"));
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .submitEvent(any(), eq(CASE_ID), anyMap());
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(eq(CASE_ID), eq("internal-change-add-gatekeeping"));

        verifyNoInteractions(notificationClient);
        verifyNoMoreInteractions(concurrencyHelper);
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

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDetails);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .startEvent(eq(CASE_ID), eq("internal-update-case-summary"));
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT)).submitEvent(any(), eq(CASE_ID), anyMap());

        // start don't finish
        verify(concurrencyHelper).startEvent(eq(CASE_ID), eq("internal-change-add-gatekeeping"));

        verifyNoInteractions(notificationClient);
        verifyNoMoreInteractions(concurrencyHelper);
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

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2))
            .startEvent(eq(CASE_ID), eventIdCaptor.capture());
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .submitEvent(startEventResponseArgumentCaptor.capture(), eq(CASE_ID), eventDataCaptor.capture());

        assertThat(eventIdCaptor.getAllValues())
            .containsExactlyInAnyOrder("internal-update-case-summary", "internal-change-add-gatekeeping");
        assertThat(startEventResponseArgumentCaptor.getAllValues().stream().map(StartEventResponse::getEventId))
            .containsExactly("internal-update-case-summary");

        verifyNoMoreInteractions(concurrencyHelper);
    }

    @ParameterizedTest
    @EnumSource(value = HearingOptions.class, names = {"EDIT_FUTURE_HEARING", "ADJOURN_HEARING", "VACATE_HEARING"})
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

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(caseDetails.toBuilder().build())
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);

        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2))
            .startEvent(eq(CASE_ID), eventIdCaptor.capture());
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT))
            .submitEvent(startEventResponseArgumentCaptor.capture(), eq(CASE_ID), eventDataCaptor.capture());

        assertThat(startEventResponseArgumentCaptor.getAllValues().stream().map(StartEventResponse::getEventId))
            .containsExactly("internal-update-case-summary");
        assertThat(eventIdCaptor.getAllValues())
            .containsExactlyInAnyOrder("internal-update-case-summary", "internal-change-add-gatekeeping");

        verifyNoMoreInteractions(concurrencyHelper);
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
            .caseSummaryNextHearingDateTime(LocalDateTime.of(hearingDate, LocalTime.of(13, 0, 0)))
            .caseSummaryCourtName(COURT_NAME)
            .caseSummaryLanguageRequirement("No")
            .caseSummaryLALanguageRequirement("No")
            .caseSummaryHighCourtCase("No")
            .caseSummaryLAHighCourtCase("No")
            .caseSummaryLATabHidden("Yes")
            .build());
    }

}
