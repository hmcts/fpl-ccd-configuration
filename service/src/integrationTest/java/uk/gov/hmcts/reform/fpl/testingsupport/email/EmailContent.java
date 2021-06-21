package uk.gov.hmcts.reform.fpl.testingsupport.email;

public class EmailContent {
    public static final String NEW_LINE = "\n";
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

    public EmailContent lines(int lines) {
        body.append(NEW_LINE.repeat(Math.max(0, lines)));
        return this;
    }

    public String body() {
        return body.toString();
    }

    public EmailContent h1(String header) {
        return line("#" + header);
    }

    public EmailContent list(String... listItems) {
        for (String item : listItems) {
            line("* " + item);
        }
        return this;
    }

    public EmailContent callout(String callout) {
        return line("^" + callout);
    }
}
