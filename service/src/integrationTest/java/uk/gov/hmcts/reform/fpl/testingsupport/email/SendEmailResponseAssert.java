package uk.gov.hmcts.reform.fpl.testingsupport.email;

import org.assertj.core.api.AbstractAssert;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendEmailResponseAssert extends AbstractAssert<SendEmailResponseAssert, SendEmailResponse> {
    private static final String URL = "https://documents\\.service\\.gov\\.uk/d/([\\w|-]+/[\\w|-]+\\?key=[\\w|-]+)";
    private static final Pattern PATTERN = Pattern.compile(URL);

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
        actualBody = cleanGovNotifyDocLink(actualBody);
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

    private String cleanGovNotifyDocLink(String body) {
        String cleaned = body;
        Matcher matcher = PATTERN.matcher(body);

        while (matcher.find()) {
            String group = matcher.group(1);
            cleaned = cleaned.replace(group, "");
        }

        return cleaned;
    }
}
