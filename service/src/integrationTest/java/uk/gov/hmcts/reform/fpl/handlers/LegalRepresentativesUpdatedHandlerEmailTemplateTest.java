package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativesDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalCounsellorEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LegalRepresentativeAddedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    LegalRepresentativesUpdatedHandler.class, LegalRepresentativeAddedContentProvider.class,
    LegalRepresentativesDifferenceCalculator.class, EmailNotificationHelper.class, CaseUrlService.class,
    LocalAuthorityRecipientsService.class, LegalCounsellorEmailContentProvider.class
})
class LegalRepresentativesUpdatedHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String CHILD_LAST_NAME = "Dorn";
    private static final String RESPONDENT_LAST_NAME = "Magnus";

    @MockBean
    private UserService userService;

    @Autowired
    private LegalRepresentativesUpdatedHandler underTest;

    @Test
    void sendToLA() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("FAM_NUM")
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .legalRepresentatives(wrapElements(LegalRepresentative.builder()
                .fullName("Leman Russ")
                .email("fake@email.com")
                .build()
            ))
            .build();

        CaseData before = CaseData.builder().build();

        underTest.sendEmailToLegalRepresentativesUpdated(new LegalRepresentativesUpdated(caseData, before));

        assertThat(response())
            .hasSubject("You’ve been added to a case, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("Dear Leman Russ")
                .line()
                .line(LOCAL_AUTHORITY_NAME + " has added you to this case:")
                .line()
                .callout(RESPONDENT_LAST_NAME + ", FAM_NUM")
                .line()
                .line("To view the case, sign in to:")
                .line("http://fake-url/cases/case-details/12345")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .line("For legal representation guidance navigate to this link:")
                .line("https://www.gov.uk/government/publications/"
                    + "myhmcts-how-to-respond-to-a-family-public-law-order-application")
                .line()
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }
}
