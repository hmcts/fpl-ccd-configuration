package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    FurtherEvidenceUploadedEventHandler.class,
    FurtherEvidenceNotificationService.class,
    FurtherEvidenceUploadedEmailContentProvider.class,
    CaseUrlService.class,
    EmailNotificationHelper.class
})
class FurtherEvidenceUploadedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final Long CASE_ID = 12345L;
    private static final String LA_EMAIL = "la@example.com";
    private static final String REP_EMAIL = "resp@example.com";
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2021, 2, 22, 0, 0, 0).plusMonths(3);
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CHILD_LAST_NAME = "Jones";

    @Autowired
    private FurtherEvidenceUploadedEventHandler underTest;

    @MockBean
    private FeatureToggleService toggleService;

    @ParameterizedTest
    @MethodSource("subjectLineSource")
    void sendNotification(boolean toggle, String name) {
        UUID representativeUUID = UUID.randomUUID();

        Representative representative = Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
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
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().lastName(CHILD_LAST_NAME).dateOfBirth(LocalDate.now()).build())
                .build()
            ))
            .id(CASE_ID)
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .build();

        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);

        underTest.handleDocumentUploadedEvent(new FurtherEvidenceUploadedEvent(
            caseData, caseDataBefore, true,
            UserDetails.builder().email(LA_EMAIL).forename("The").surname("Sender").build()
        ));

        assertThat(response())
            .hasSubject("New documents uploaded, " + name)
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

    private static Stream<Arguments> subjectLineSource() {
        return Stream.of(
            Arguments.of(true, CHILD_LAST_NAME),
            Arguments.of(false, RESPONDENT_LAST_NAME)
        );
    }
}
