package uk.gov.hmcts.reform.fpl.controllers.documents;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

abstract class ManageDocumentsControllerSubmittedBaseTest extends AbstractCallbackTest {
    protected static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    protected static final String REP_1_EMAIL = "rep1@example.com";
    protected static final String RESPONDENT_SOLICITOR_1_EMAIL = "respondent1@solicitor.com";
    protected static final String UNREGISTERED_RESPONDENT_SOLICITOR_2_EMAIL = "respondent2@solicitor.com";
    protected static final String CHILD_SOLICITOR_1_EMAIL = "child1@solicitor.com";
    protected static final String UNREGISTERED_CHILD_SOLICITOR_2_EMAIL = "child2@solicitor.com";

    protected ManageDocumentsControllerSubmittedBaseTest(String eventName) {
        super(eventName);
    }

    protected CallbackRequest buildCallbackRequestForAddingApplicationDocument(final boolean confidential) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildData(buildApplicationDocument(confidential)))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected CallbackRequest buildCallbackRequestForAddingAnyOtherDocuments(final String bundleName,
                                                                             final boolean confidential) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildData(bundleName, buildEvidenceBundle(confidential)))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected CallbackRequest buildCallbackRequestForAddingCourtBundle() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildData(buildCourtBundle()))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected CallbackRequest buildCallbackRequestForAddingRespondentStatement(boolean confidential) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildData(buildRespondentStatement(confidential)))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected static ApplicationDocument buildApplicationDocument(boolean confidential) {
        ApplicationDocument.ApplicationDocumentBuilder document
            = ApplicationDocument.builder()
            .documentType(ApplicationDocumentType.BIRTH_CERTIFICATE)
            .uploadedBy("user who uploaded")
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference());

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    protected static CourtBundle buildCourtBundle() {
        return CourtBundle.builder()
            .document(DocumentReference.builder()
                .filename("filename")
                .url(randomAlphanumeric(10))
                .binaryUrl(randomAlphanumeric(10))
                .build())
            .hearing("hearing")
            .dateTimeUploaded(LocalDateTime.now())
            .uploadedBy("LA")
            .build();
    }

    protected static RespondentStatement buildRespondentStatement(boolean confidential) {
        RespondentStatement.RespondentStatementBuilder document
            = RespondentStatement.builder().respondentName("Timothy Jones")
            .supportingEvidenceBundle(wrapElements(buildEvidenceBundle(confidential)));
        return document.build();
    }

    protected static SupportingEvidenceBundle buildEvidenceBundle(
        boolean confidential) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name("dummy document")
            .uploadedBy("user who uploaded")
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference())
            .type(FurtherEvidenceType.GUARDIAN_REPORTS);

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    protected Map<String, Object> buildData(ApplicationDocument applicationDocument) {
        return Map.of(
            "localAuthorities", wrapElements(
                LocalAuthority.builder()
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build(),
                LocalAuthority.builder()
                    .designated(NO.getValue())
                    .email(LOCAL_AUTHORITY_2_INBOX)
                    .build()),
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", buildRepresentatives(),
            "children1", buildChildren1(),
            "respondents1", buildRespondents1(),
            "applicationDocuments", wrapElements(applicationDocument)
        );
    }

    protected Map<String, Object> buildData(String bundleName, SupportingEvidenceBundle supportingEvidenceBundle) {
        return Map.of(
            "localAuthorities", wrapElements(
                LocalAuthority.builder()
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build(),
                LocalAuthority.builder()
                    .designated(NO.getValue())
                    .email(LOCAL_AUTHORITY_2_INBOX)
                    .build()),
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", buildRepresentatives(),
            "children1", buildChildren1(),
            "respondents1", buildRespondents1(),
            bundleName, wrapElements(supportingEvidenceBundle)
        );
    }

    protected Map<String, Object> buildData(RespondentStatement... respondentStatements) {
        return Map.of(
            "localAuthorities", wrapElements(
                LocalAuthority.builder()
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build(),
                LocalAuthority.builder()
                    .designated(NO.getValue())
                    .email(LOCAL_AUTHORITY_2_INBOX)
                    .build()),
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", buildRepresentatives(),
            "children1", buildChildren1(),
            "respondents1", buildRespondents1(),
            "respondentStatements", wrapElements(respondentStatements)
        );
    }

    protected Map<String, Object> buildData(CourtBundle... courtBundle) {
        return Map.of(
            "localAuthorities", wrapElements(
                LocalAuthority.builder()
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build(),
                LocalAuthority.builder()
                    .designated(NO.getValue())
                    .email(LOCAL_AUTHORITY_2_INBOX)
                    .build()),
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", buildRepresentatives(),
            "children1", buildChildren1(),
            "respondents1", buildRespondents1(),
            "courtBundleList", wrapElements(courtBundle)
        );
    }

    protected void verifySendingNotificationToAllParties(NotificationClient notificationClient, String templateId, long caseId)
        throws NotificationClientException {
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(notificationReference(caseId)));

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(notificationReference(caseId)));

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(),
            eq(notificationReference(caseId)));

        // registered respondent solicitor
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(RESPONDENT_SOLICITOR_1_EMAIL),
            anyMap(),
            eq(notificationReference(caseId)));

        // unregistered respondent solicitor
        verify(notificationClient, never()).sendEmail(
            eq(templateId),
            eq(UNREGISTERED_RESPONDENT_SOLICITOR_2_EMAIL),
            anyMap(),
            eq(notificationReference(caseId)));

        // registered child solicitor
        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(CHILD_SOLICITOR_1_EMAIL),
            anyMap(),
            eq(notificationReference(caseId)));

        // unregistered child solicitor
        verify(notificationClient, never()).sendEmail(
            eq(templateId),
            eq(UNREGISTERED_CHILD_SOLICITOR_2_EMAIL),
            anyMap(),
            eq(notificationReference(caseId)));
    }

    private List<Element<Representative>> buildRepresentatives() {
        return wrapElements(Representative.builder()
            .email(REP_1_EMAIL)
            .fullName("Representative Snow")
            .role(REPRESENTING_RESPONDENT_1)
            .servingPreferences(DIGITAL_SERVICE)
            .build());
    }

    private List<Element<Respondent>> buildRespondents1() {
        return wrapElements(
            Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName("Jones")
                        .relationshipToChild("Father")
                        .build())
                .solicitor(RespondentSolicitor.builder()
                    .email(RESPONDENT_SOLICITOR_1_EMAIL)
                    .organisation(Organisation.builder().organisationID("ABC").build())
                    .build())
                .build(),
            Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Sarah")
                        .lastName("Simpson")
                        .relationshipToChild("Mother")
                        .build())
                .solicitor(RespondentSolicitor.builder()
                    .email(UNREGISTERED_RESPONDENT_SOLICITOR_2_EMAIL)
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().build())
                    .build())
                .build()
        );
    }

    private List<Element<Child>> buildChildren1() {
        return wrapElements(
            Child.builder().party(
                    ChildParty.builder()
                        .firstName("Chris")
                        .lastName("Law")
                        .build())
                .solicitor(RespondentSolicitor.builder()
                    .email(CHILD_SOLICITOR_1_EMAIL)
                    .organisation(Organisation.builder().organisationID("ABC").build())
                    .build())
                .build(),
            Child.builder().party(
                    ChildParty.builder()
                        .firstName("Bethia")
                        .lastName("Law")
                        .build())
                .solicitor(RespondentSolicitor.builder()
                    .email(UNREGISTERED_CHILD_SOLICITOR_2_EMAIL)
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().build())
                    .build())
                .build());
    }
}
