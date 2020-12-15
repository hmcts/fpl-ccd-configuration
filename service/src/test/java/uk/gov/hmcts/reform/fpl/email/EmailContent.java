package uk.gov.hmcts.reform.fpl.email;

public class EmailContent {
    private static final String NEW_LINE = "\r\n";
    private final StringBuilder body = new StringBuilder();

    public static EmailContent emailContent() {
        return new EmailContent();
    }

    public EmailContent start() {
        return this;
    }

    public EmailContent end(String line) {
        body.append(line);
        return this;
    }

    public EmailContent line(String line) {
        body.append(line).append(NEW_LINE);
        return this;
    }

    public EmailContent line() {
        body.append(NEW_LINE);
        return this;
    }

    public String body() {
        return body.toString();
    }
}
