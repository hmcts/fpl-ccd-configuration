package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    FurtherEvidenceUploadedEventHandler.class,
    FurtherEvidenceUploadedEvent.class,
    FurtherEvidenceNotificationService.class,
    FurtherEvidenceUploadedEmailContentProvider.class,
    LocalAuthorityEmailLookupConfiguration.class,
    InboxLookupService.class,
    NotificationService.class,
    ObjectMapper.class,
    CaseUrlService.class
})
class FurtherEvidenceUploadedEmailTemplateTest extends EmailTemplateTest {
    private static final Long CASE_ID = 12345L;
    private static final String LA_EMAIL = "la@example.com";
    private static final String REP_EMAIL = "resp@example.com";
    private static final LocalDateTime HEARING_DATE =
        LocalDateTime.of(2021, 2, 22, 0, 0, 0).plusMonths(3);

    @Autowired
    private FurtherEvidenceUploadedEventHandler underTest;

    @Test
    void sendNotification() {
        UUID representativeUUID = UUID.randomUUID();

        Representative representative = Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().lastName("Smith").build())
            .representedBy(wrapElements(List.of(representativeUUID)))
            .build();

        CaseData caseData = CaseData.builder()
            .furtherEvidenceDocumentsLA(
                wrapElements(SupportingEvidenceBundle.builder()
                    .name("Non-Confidential Evidence Document 1")
                    .uploadedBy("LA")
                    .dateTimeUploaded(LocalDateTime.now())
                    .document(DocumentReference.builder().build())
                    .build()))
            .familyManCaseNumber(CASE_ID.toString())
            .representatives(List.of(element(representativeUUID, representative)))
            .respondents1(wrapElements(respondent))
            .id(CASE_ID)
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.handleDocumentUploadedEvent(
            new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, true,
                UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()));

        assertThat(response())
            .hasSubject("New documents uploaded, Smith")
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
}
