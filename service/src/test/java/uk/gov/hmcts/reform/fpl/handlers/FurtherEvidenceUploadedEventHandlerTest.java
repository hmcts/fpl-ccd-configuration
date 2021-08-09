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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceUploadedEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    private static final String LA_USER = "LA";
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String HMCTS_USER_EMAIL = "hmcts@examaple.com";
    private static final String REP_SOLICITOR_USER_EMAIL = "rep@examaple.com";
    private static final String SENDER_FORENAME = "The";
    private static final String SENDER_SURNAME = "Sender";
    private static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);
    private static final String NON_CONFIDENTIAL_1 = "non-confidential-1";
    private static final String NON_CONFIDENTIAL_2 = "non-confidential-2";
    private static final String CONFIDENTIAL_1 = "confidential-doc-1";
    private static final List<String> NON_CONFIDENTIAL = buildNonConfidentialDocumentsNamesList();
    private static final List<String> CONFIDENTIAL = buildConfidentialDocumentsNamesList();

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @InjectMocks
    private FurtherEvidenceUploadedEventHandler furtherEvidenceUploadedEventHandler;

    @Mock
    private UserService userService;

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                true,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL), SENDER,
            NON_CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByLA() {
        CaseData caseDataBefore = buildCaseDataWithConfidentialLADocuments();

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        CaseData caseData = commonCaseBuilder().furtherEvidenceDocumentsLA(
            wrapElements(createDummyEvidenceBundle(CONFIDENTIAL_1, LA_USER, false))
        ).build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                true,
                userDetailsLA());

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL), SENDER, CONFIDENTIAL);
    }

    @Test
    void shouldSendNotificationWhenDocIsMadeNonConfidentialByHMCTS() {
        CaseData caseDataBefore = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        // buildCaseDataWithConfidentialLADocuments() has a "confidential-doc-1" marked as confidential
        // so we are creating a new list with "confidential-doc-1" not marked as confidential
        CaseData caseData = commonCaseBuilder().furtherEvidenceDocuments(
            wrapElements(createDummyEvidenceBundle(CONFIDENTIAL_1, HMCTS_USER, false))
        ).build();

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));
        when(userService.hasAnyCaseRoleFrom(any(), any())).thenReturn(false);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                false,
                userDetailsHMCTS());
        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER,
            CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithConfidentialLADocuments();
        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                true,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreRemoved() {
        CaseData caseData = commonCaseBuilder().build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialLADocuments(),
                true,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenDocumentsAreSame() {
        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseData,
                true,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByHMCTS() {
        CaseData caseData = buildCaseDataWithNonConfidentialDocuments(HMCTS_USER);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));
        when(userService.hasAnyCaseRoleFrom(any(), any())).thenReturn(false);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocuments(HMCTS_USER),
                false,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService).sendNotification(
            caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER, NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByHMCTS() {
        CaseData caseData = buildCaseDataWithConfidentialDocuments(HMCTS_USER);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(HMCTS_USER),
                false,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    void shouldSendNotificationWhenNonConfidentialDocIsUploadedByRespSolicitor() {
        CaseData caseData = buildCaseDataWithNonConfidentialDocumentsSolicitor(REP_USER);

        when(furtherEvidenceNotificationService.getRepresentativeEmails(caseData))
            .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL));
        when(furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL));
        when(userService.hasAnyCaseRoleFrom(any(), any())).thenReturn(true);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialDocumentsSolicitor(REP_USER),
                false,
                userDetailsRespondentSolicitor());
        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService)
            .sendNotification(caseData, Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL), SENDER,
                NON_CONFIDENTIAL);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialDocIsUploadedByRespSolicitor() {
        // This test is here even though the UI does not allow this scenario but code has a path to make it possible
        CaseData caseData = buildCaseDataWithConfidentialDocuments(REP_USER);
        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithNonConfidentialDocuments(REP_USER),
                false,
                userDetailsRespondentSolicitor());

        when(userService.hasAnyCaseRoleFrom(any(), any())).thenReturn(true);

        furtherEvidenceUploadedEventHandler.handleDocumentUploadedEvent(furtherEvidenceUploadedEvent);

        verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
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

    private CaseData buildCaseDataWithNonConfidentialDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
                buildNonConfidentialDocumentList(uploadedBy))
            .build();
    }

    private CaseData buildCaseDataWithConfidentialDocuments(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocuments(
                buildConfidentialDocumentList(uploadedBy))
            .build();
    }

    private CaseData buildCaseDataWithConfidentialDocumentsSolicitor(final String uploadedBy) {
        return commonCaseBuilder()
            .furtherEvidenceDocumentsSolicitor(
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
            createDummyEvidenceBundle(NON_CONFIDENTIAL_1, uploadedBy, false),
            createDummyEvidenceBundle(NON_CONFIDENTIAL_2, uploadedBy, false));
    }

    private static final List<String> buildNonConfidentialDocumentsNamesList() {
        List<String> documents = new ArrayList<>();
        documents.add(NON_CONFIDENTIAL_1);
        documents.add(NON_CONFIDENTIAL_2);
        return documents;
    }

    private static final List<String> buildConfidentialDocumentsNamesList() {
        List<String> documents = new ArrayList<>();
        documents.add(CONFIDENTIAL_1);
        return documents;
    }

    private CaseData.CaseDataBuilder commonCaseBuilder() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()));
    }

    private UserDetails userDetailsLA() {
        return UserDetails.builder().email(LA_USER_EMAIL).forename(SENDER_FORENAME).surname(SENDER_SURNAME).build();
    }

    private UserDetails userDetailsHMCTS() {
        return UserDetails.builder().email(HMCTS_USER_EMAIL).forename(SENDER_FORENAME).surname(SENDER_SURNAME).build();
    }

    private UserDetails userDetailsRespondentSolicitor() {
        return UserDetails.builder()
            .email(REP_SOLICITOR_USER_EMAIL)
            .forename(SENDER_FORENAME)
            .surname(SENDER_SURNAME)
            .build();
    }
}
