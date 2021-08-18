package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.LOCAL_AUTHORITY;
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
    private static final List<RepresentativeRole.Type> ROLES = List.of(CAFCASS, RESPONDENT);
    private static final UUID REPRESENTATIVE_UUID = UUID.randomUUID();
    private static final CaseData CASE_DATA = buildCaseData();
    private static final CaseData CASE_DATA_BEFORE = buildCaseDataBefore();

    @Autowired
    private FurtherEvidenceUploadedEventHandler underTest;

    @Autowired
    private RepresentativesInbox representativesInbox;

    @MockBean
    private SendDocumentService sendDocumentService;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void sendNotificationWhenFeatureToggleOff() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);
        when(representativesInbox.getRepresentativeEmailsFilteredByRole(CASE_DATA, DIGITAL_SERVICE, ROLES))
            .thenReturn(newHashSet("resp@example.com"));

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            CASE_DATA, CASE_DATA_BEFORE, LOCAL_AUTHORITY,
            UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
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
                    + " If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void sendNotificationWhenFeatureToggleOn() {

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(representativesInbox.getRepresentativeEmailsFilteredByRole(CASE_DATA, DIGITAL_SERVICE, ROLES))
            .thenReturn(newHashSet("resp@example.com"));

        underTest.sendDocumentsUploadedNotification(new FurtherEvidenceUploadedEvent(
            CASE_DATA, CASE_DATA_BEFORE, LOCAL_AUTHORITY,
            UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
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
                    + " If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    private static CaseData buildCaseData() {
        return CaseData.builder()
            .furtherEvidenceDocumentsLA(
                wrapElements(SupportingEvidenceBundle.builder()
                    .name("Non-Confidential Evidence Document 1")
                    .uploadedBy("LA")
                    .dateTimeUploaded(LocalDateTime.now())
                    .document(DocumentReference.builder().build())
                    .build()))
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

    private static List<Element<Representative>>  buildRepresentativesList() {
        return List.of(element(REPRESENTATIVE_UUID, Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(DIGITAL_SERVICE)
            .build()));
    }

    private static Respondent buildRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .representedBy(wrapElements(List.of(REPRESENTATIVE_UUID)))
            .build();
    }
}
