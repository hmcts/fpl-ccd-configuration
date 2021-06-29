package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;

import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;

@SpringBootTest(classes = {
    FailedPBAPaymentEventHandler.class,
    LocalAuthorityEmailLookupConfiguration.class,
    NotificationService.class,
    CtscEmailLookupConfiguration.class,
    FailedPBAPaymentContentProvider.class,
    ObjectMapper.class,
    CaseUrlService.class
})
class FailedPBAPaymentEventHandlerEmailTemplateTest extends EmailTemplateTest {

    @Autowired
    private FailedPBAPaymentEventHandler underTest;

    @Test
    void notifyCTSCC2() {
        underTest.notifyCTSC(new FailedPBAPaymentEvent(
            CaseData.builder().id(123L).build(), C2_APPLICATION, ""
        ));

        assertThat(response())
            .hasSubject("C2 – payment not taken")
            .hasBody(emailContent()
                .line("The online payment has failed for:")
                .line()
                .callout("http://fake-url/cases/case-details/123#C2")
                .line()
                .end("Contact the relevant court so they can take the payment.")
            );
    }

    @Test
    void notifyCTSCC110A() {
        underTest.notifyCTSC(new FailedPBAPaymentEvent(
            CaseData.builder().id(123L).build(), C110A_APPLICATION, ""
        ));

        assertThat(response())
            .hasSubject("C110a – payment not taken")
            .hasBody(emailContent()
                .line("The online payment has failed for:")
                .line()
                .callout("http://fake-url/cases/case-details/123")
                .line()
                .end("Contact the relevant court so they can take the payment.")
            );
    }

    @Test
    void notifyLocalAuthorityC2() {
        underTest.notifyLocalAuthority(new FailedPBAPaymentEvent(
            CaseData.builder().id(123L).build(), C2_APPLICATION, ""
        ));

        assertThat(response())
            .hasSubject("C2 – payment not taken")
            .hasBody(emailContent()
                .line("Your C2 application has been sent to the court.")
                .line()
                .line("However, the online payment did not work. We’ll contact you soon to take payment.")
                .line()
                .line("This will not affect the progress of your application.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. "
                    + "If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifyLocalAuthorityC110A() {
        underTest.notifyLocalAuthority(new FailedPBAPaymentEvent(
            CaseData.builder().id(123L).build(), C110A_APPLICATION, ""
        ));

        assertThat(response())
            .hasSubject("C110a – payment not taken")
            .hasBody(emailContent()
                .line("Your C110a application has been sent to the court.")
                .line()
                .line("However, the online payment did not work. We’ll contact you soon to take payment.")
                .line()
                .line("This will not affect the progress of your application.")
                .line()
                .line("HM Courts & Tribunals Service")
                .line()
                .end("Do not reply to this email. "
                    + "If you need to contact us, call 0330 808 4424 or email contactfpl@justice.gov.uk")
            );
    }
}
