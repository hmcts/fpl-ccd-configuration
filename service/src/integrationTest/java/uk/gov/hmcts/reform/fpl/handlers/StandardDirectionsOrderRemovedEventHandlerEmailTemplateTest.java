package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    StandardDirectionsOrderRemovedEventHandler.class, OrderRemovalEmailContentProvider.class,
    EmailNotificationHelper.class, CaseUrlService.class
})
class StandardDirectionsOrderRemovedEventHandlerEmailTemplateTest extends EmailTemplateTest {
    private static final String CHILD_LAST_NAME = "yet another name";
    private static final String RESPONDENT_LAST_NAME = "that I cannot be bothered to think of";
    private static final long CASE_ID = 123456L;

    @Autowired
    private StandardDirectionsOrderRemovedEventHandler underTest;

    @Test
    void notifyGatekeeper() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
                .build()))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
                .build()))
            .gatekeeperEmails(wrapElements(EmailAddress.builder().email("some@email.here").build()))
            .build();
        String reason = "Some reason";

        underTest.notifyGatekeeperOfRemovedSDO(new StandardDirectionsOrderRemovedEvent(caseData, reason));

        assertThat(response())
            .hasSubject("Gatekeeping order removed, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("We've removed the gatekeeping order for the case:")
                .line()
                .callout(String.valueOf(CASE_ID))
                .line()
                .h1("Why the order was removed")
                .line()
                .line(reason)
                .line()
                .line("To view the case, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/123456")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }
}
