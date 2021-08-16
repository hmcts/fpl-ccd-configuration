package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    FurtherEvidenceUploadedEventHandler.class, FurtherEvidenceNotificationService.class,
    FurtherEvidenceUploadedEmailContentProvider.class, CaseUrlService.class, EmailNotificationHelper.class
})
class FurtherEvidenceUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final Long CASE_ID = 12345L;
    private static final String LA_EMAIL = "la@example.com";
    private static final String REP_EMAIL = "resp@example.com";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2021, 2, 22, 0, 0, 0).plusMonths(3);
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CHILD_LAST_NAME = "Jones";
    private static final UUID REPRESENTATIVE_UUID = UUID.randomUUID();
    private static final CaseData caseData = buildCaseData();
    private static final CaseData caseDataBefore = buildCaseDataBefore();
    private static final EmailContent EMAIL_CONTENT_DOC_NAMES = buildEmailContentDocumentNames();
    private static final EmailContent EMAIL_CONTENT_NO_DOC_NAMES = buildEmailContentNoDocumentNames();

    @Autowired
    private FurtherEvidenceUploadedEventHandler underTest;

    @MockBean
    private SendDocumentService sendDocumentService;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void sendNotificationWhenFeatureToggleOffForLAUser() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            caseData, caseDataBefore, LOCAL_AUTHORITY,
            UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(EMAIL_CONTENT_NO_DOC_NAMES);
    }

    @Test
    void sendNotificationWhenFeatureToggleOffForSolicitorUser() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            caseData, caseDataBefore, SOLICITOR,
            UserDetails.builder().email(REP_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(EMAIL_CONTENT_NO_DOC_NAMES);
    }

    @Test
    void sendNotificationWhenFeatureToggleOnForLAUser() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            caseData, caseDataBefore, LOCAL_AUTHORITY,
            UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(EMAIL_CONTENT_DOC_NAMES);
    }

    @Test
    void sendNotificationWhenFeatureToggleOnForSolicitor() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            caseData, caseDataBefore, SOLICITOR,
            UserDetails.builder().email(REP_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(EMAIL_CONTENT_DOC_NAMES);
    }

    private static CaseData buildCaseData() {
        return CaseData.builder()
            .furtherEvidenceDocumentsLA(buildSupportingEvidenceBundle("LA"))
            .respondentStatements(buildRespondentStatementsList())
            .familyManCaseNumber(CASE_ID.toString())
            .representatives(buildRepresentativesList())
            .respondents1(wrapElements(buildRespondent()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().lastName(CHILD_LAST_NAME).dateOfBirth(LocalDate.now()).build())
                .build()
            ))
            .id(CASE_ID)
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .build();
    }

    private static CaseData buildCaseDataBefore() {
        return CaseData.builder().id(CASE_ID).build();
    }

    private static List<Element<RespondentStatement>> buildRespondentStatementsList() {
        return wrapElements(RespondentStatement.builder()
            .respondentName("NAME")
            .respondentId(UUID.randomUUID())
            .supportingEvidenceBundle(buildSupportingEvidenceBundle("REP")).build());
    }

    private static List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(String uploadedBy) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("Non-Confidential Evidence Document 1")
            .uploadedBy(uploadedBy)
            .dateTimeUploaded(LocalDateTime.now())
            .document(DocumentReference.builder().build())
            .build());
    }

    private static List<Element<Representative>>  buildRepresentativesList() {
        return List.of(element(REPRESENTATIVE_UUID, Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build()));
    }

    private static Respondent buildRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .representedBy(wrapElements(List.of(REPRESENTATIVE_UUID)))
            .build();
    }

    private static EmailContent buildEmailContentDocumentNames() {
        return emailContent()
            .line("The Sender has uploaded documents for:")
            .line()
            .callout("Smith, 12345, hearing 22 May 2021")
            .line()
            .h1("Documents uploaded")
            .line()
            .line()
            .list("Non-Confidential Evidence Document 1")
            .line()
            .line("To view them, sign in to:")
            .line()
            .line("http://fake-url/cases/case-details/12345#Documents")
            .line()
            .line("HM Courts & Tribunals Service")
            .line()
            .end("Do not reply to this email."
                + " If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk");
    }

    private static EmailContent buildEmailContentNoDocumentNames() {
        return emailContent()
            .line("The Sender has uploaded evidence documents for:")
            .line()
            .callout("Smith, 12345, hearing 22 May 2021")
            .line()
            .line("To view them, sign in to:")
            .line()
            .line("http://fake-url/cases/case-details/12345#Documents")
            .line()
            .line("HM Courts & Tribunals Service")
            .line()
            .end("Do not reply to this email."
                + " If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk");
    }
}
