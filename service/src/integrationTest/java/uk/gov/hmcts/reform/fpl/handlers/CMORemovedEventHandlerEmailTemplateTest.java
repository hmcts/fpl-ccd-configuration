package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.handlers.cmo.CMORemovedEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {
    CMORemovedEventHandler.class,
    OrderRemovalEmailContentProvider.class,
    EmailNotificationHelper.class,
    CaseUrlService.class
})
class CMORemovedEventHandlerEmailTemplateTest extends EmailTemplateTest {

    private static final String REMOVAL_REASON = "reason";
    private static final Long CASE_ID = 12345L;
    private static final String CHILD_LAST_NAME = "Holmes";
    private static final String RESPONDENT_LAST_NAME = "Watson";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .children1(wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_LAST_NAME).build())
            .build()))
        .build();

    @Autowired
    private CMORemovedEventHandler underTest;

    @Test
    void notifyLA() {
        underTest.notifyLocalAuthorityOfRemovedCMO(new CMORemovedEvent(CASE_DATA, REMOVAL_REASON));

        assertThat(response())
            .hasSubject("Case management order removed, " + CHILD_LAST_NAME)
            .hasBody(emailContent()
                .line("We've removed the case management order for the case:")
                .line()
                .callout(String.valueOf(CASE_ID))
                .line()
                .h1("Why the order was removed")
                .line()
                .line(REMOVAL_REASON)
                .line()
                .line("To view the case, sign in to:")
                .line()
                .line("http://fake-url/cases/case-details/" + CASE_ID)
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                      + "contactfpl@justice.gov.uk")
            );
    }
}
