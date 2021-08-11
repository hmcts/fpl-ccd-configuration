package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceUploadedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    private static final String LA_USER = "LA";
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String DESIGNATED_LA_USER_1_EMAIL = "designated1@examaple.com";
    private static final String DESIGNATED_LA_USER_2_EMAIL = "designated2@examaple.com";
    private static final String SECONDARY_LA_USER_1_EMAIL = "secondary1@examaple.com";
    private static final String SECONDARY_LA_USER_2_EMAIL = "secondary2@examaple.com";
    private static final String HMCTS_USER_EMAIL = "hmcts@examaple.com";
    private static final String REP_SOLICITOR_USER_EMAIL = "rep@examaple.com";
    private static final String SENDER_FORENAME = "The";
    private static final String SENDER_SURNAME = "Sender";
    private static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FurtherEvidenceUploadedEventHandler furtherEvidenceUploadedEventHandler;

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByDesignatedLA() {
        final CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                true);

        when(userService.getUserDetails()).thenReturn(userDetailsLA(DESIGNATED_LA_USER_1_EMAIL));
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(LASOLICITOR));
        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(
                REP_SOLICITOR_1_EMAIL,
                REP_SOLICITOR_2_EMAIL),
            SENDER);

        verifyNoMoreInteractions(furtherEvidenceNotificationService, userService);
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedBySecondaryLA() {
        final CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                true);

        when(userService.getUserDetails()).thenReturn(userDetailsLA(SECONDARY_LA_USER_1_EMAIL));
        when(userService.getCaseRoles(CASE_ID)).thenReturn(Set.of(LASHARED));
        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(DESIGNATED_LA_USER_1_EMAIL, DESIGNATED_LA_USER_2_EMAIL, SECONDARY_LA_USER_2_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(
                DESIGNATED_LA_USER_1_EMAIL,
                DESIGNATED_LA_USER_2_EMAIL,
                SECONDARY_LA_USER_2_EMAIL,
                REP_SOLICITOR_1_EMAIL,
                REP_SOLICITOR_2_EMAIL),
            SENDER);

        verifyNoMoreInteractions(furtherEvidenceNotificationService, userService);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByLA() {
        CaseData caseDataBefore = buildCaseDataWithConfidentialLADocuments();

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        CaseData caseData = commonCaseBuilder().furtherEvidenceDocumentsLA(
            wrapElements(createDummyEvidenceBundle("confidential-doc-1", LA_USER, false))
        ).build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                true);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        when(userService.getUserDetails()).thenReturn(userDetailsLA(DESIGNATED_LA_USER_1_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL), SENDER);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByHMCTS() {
        final CaseData caseDataBefore = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        final CaseData caseData = commonCaseBuilder().furtherEvidenceDocuments(
            wrapElements(createDummyEvidenceBundle("confidential-doc-1", HMCTS_USER, false)))
            .build();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                false);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(SECONDARY_LA_USER_1_EMAIL));

        when(userService.getUserDetails()).thenReturn(userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, SECONDARY_LA_USER_1_EMAIL), SENDER);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByLA() {
        final CaseData caseData = buildCaseDataWithConfidentialLADocuments();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                true);

        when(userService.getUserDetails()).thenReturn(userDetailsLA(DESIGNATED_LA_USER_1_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreRemoved() {
        final CaseData caseData = commonCaseBuilder().build();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                true);

        when(userService.getUserDetails()).thenReturn(userDetailsLA(DESIGNATED_LA_USER_1_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreSame() {
        final CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseData,
                true);

        when(userService.getUserDetails()).thenReturn(userDetailsLA(DESIGNATED_LA_USER_1_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByHMCTS() {
        final CaseData caseData = buildCaseDataWithNonConfidentialDocuments(HMCTS_USER);

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(HMCTS_USER),
                false);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(SECONDARY_LA_USER_1_EMAIL));
        when(userService.getUserDetails()).thenReturn(userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, SECONDARY_LA_USER_1_EMAIL), SENDER);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByHMCTS() {
        final CaseData caseData = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(HMCTS_USER),
                false);

        when(userService.getUserDetails()).thenReturn(userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByRespSolicitor() {
        final CaseData caseData = buildCaseDataWithNonConfidentialDocuments(REP_USER);

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(REP_USER),
                false);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(SECONDARY_LA_USER_1_EMAIL));
        when(userService.getUserDetails()).thenReturn(userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService)
            .sendNotification(caseData,
                Set.of(
                    REP_SOLICITOR_1_EMAIL,
                    REP_SOLICITOR_2_EMAIL,
                    SECONDARY_LA_USER_1_EMAIL),
                SENDER);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByRespSolicitor() {
        // This test is here even though the UI does not allow this scenario but code has a path to make it possible
        final CaseData caseData = buildCaseDataWithConfidentialDocuments(REP_USER);

        final FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(REP_USER),
                false);

        when(userService.getUserDetails()).thenReturn(userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any());
    }

    private CaseData buildCaseDataWithNonConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(
                buildNonConfidentialDocumentList(LA_USER))
            .build();
    }

    private CaseData buildCaseDataWithConfidentialLADocuments() {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsLA(
                buildConfidentialDocumentList(LA_USER))
            .build();
    }

    private CaseData buildCaseDataWithNonConfidentialDocuments(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocuments(
                buildNonConfidentialDocumentList(uploadedBy))
            .build();
    }

    private CaseData buildCaseDataWithConfidentialDocuments(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocuments(
                buildConfidentialDocumentList(uploadedBy))
            .build();
    }

    private static List<Element<SupportingEvidenceBundle>> buildConfidentialDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle("confidential-1", uploadedBy, true),
            createDummyEvidenceBundle("confidential-2", uploadedBy, true));
    }

    private static SupportingEvidenceBundle createDummyEvidenceBundle(final String name, final String uploadedBy,
                                                                      boolean confidential) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name(name)
            .uploadedBy(uploadedBy)
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference());

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    private List<Element<SupportingEvidenceBundle>> buildNonConfidentialDocumentList(final String uploadedBy) {
        return wrapElements(
            createDummyEvidenceBundle("non-confidential-1", uploadedBy, false),
            createDummyEvidenceBundle("non-confidential-2", uploadedBy, false));
    }

    private CaseData.CaseDataBuilder commonCaseBuilder() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()));
    }

    private UserDetails userDetailsLA(String userEmail) {
        return UserDetails.builder()
            .email(userEmail)
            .forename(SENDER_FORENAME)
            .surname(SENDER_SURNAME)
            .build();
    }

    private UserDetails userDetailsHMCTS() {
        return UserDetails.builder()
            .email(HMCTS_USER_EMAIL)
            .forename(SENDER_FORENAME)
            .surname(SENDER_SURNAME)
            .build();
    }

    private UserDetails userDetailsRespondentSolicitor() {
        return UserDetails.builder()
            .email(REP_SOLICITOR_USER_EMAIL)
            .forename(SENDER_FORENAME)
            .surname(SENDER_SURNAME)
            .build();
    }
}
