package uk.gov.hmcts.reform.fpl.testingsupport.email;

import org.assertj.core.api.AbstractAssert;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Objects;

public class SendEmailResponseAssert extends AbstractAssert<SendEmailResponseAssert, SendEmailResponse> {

    public SendEmailResponseAssert hasSubject(String expectedSubject) {
        isNotNull();
        String actualSubject = actual.getSubject();
        if (!Objects.equals(actualSubject, expectedSubject)) {
            failWithMessage("\nExpecting Subject to be\n<%s>\nbut was:\n<%s>",
                expectedSubject, actualSubject
            );
        }
        return this;
    }

    public SendEmailResponseAssert hasBody(EmailContent emailContent) {
        isNotNull();
        String actualBody = actual.getBody().replaceAll("\\P{Print}", "");
        String expectedBody = emailContent.body().replaceAll("\\P{Print}", "");
        if (!Objects.equals(actualBody, expectedBody)) {
            failWithMessage("\nExpecting Body to be\n<%s>\nbut was:\n<%s>",
                expectedBody, actualBody
            );
        }
        return this;
    }

    public SendEmailResponseAssert(SendEmailResponse actual) {
        super(actual, SendEmailResponseAssert.class);
    }

    public static SendEmailResponseAssert assertThat(SendEmailResponse actual) {
        return new SendEmailResponseAssert(actual);
    }

}
