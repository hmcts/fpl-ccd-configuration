package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.UndeliveredEmailsFound;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.UndeliveredEmailsContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;

@SpringBootTest(classes = {
    ObjectMapper.class,
    CaseUrlService.class,
    NotificationService.class,
    CtscEmailLookupConfiguration.class,
    UndeliveredEmailsFoundHandler.class,
    UndeliveredEmailsContentProvider.class})
class UndeliveredEmailsFoundHandlerEmailTemplateTest extends EmailTemplateTest {

    @Autowired
    private UndeliveredEmailsFoundHandler underTest;

    @Test
    void shouldSendUndeliveredEmailsReportToCtsc() {

        UndeliveredEmail undeliveredEmail1 = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Subject 1")
            .reference("prod/1234567898765432")
            .build();

        UndeliveredEmail undeliveredEmail2 = UndeliveredEmail.builder()
            .recipient("test2@test.com")
            .subject("Subject 2")
            .reference("prod/test")
            .build();

        UndeliveredEmailsFound event = new UndeliveredEmailsFound(List.of(undeliveredEmail1, undeliveredEmail2));

        underTest.sendUndeliveredEmailsReport(event);

        assertThat(response())
            .hasSubject("Email failure list")
            .hasBody(emailContent()
                .line("In the last 24 hours, the following orders failed to send:")
                .line()
                .line("To: test1@test.com")
                .line("Subject: Subject 1")
                .line("Case id: 1234567898765432")
                .line()
                .line("To: test2@test.com")
                .line("Subject: Subject 2")
                .end("Reference: test"));
    }
}
