package uk.gov.hmcts.reform.fpl.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import static java.lang.String.join;
import static java.util.Set.of;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {
    private static final String FAMILY_MAN_CASE_NUMBER = randomAlphabetic(12);
    private static final String EMAIL_TO = "recipient@example.com";
    private static final String EMAIL_FROM = "no-reply@exaple.com";
    private static final String EMAIL_SUBJECT = join("", "CaseSubmitted_", FAMILY_MAN_CASE_NUMBER);
    private static final byte[] EMAIL_ATTACHMENT_CONTENT = "1, 2, 3, 4, 5, 6".getBytes();

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService(javaMailSender);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
    }

    @Test
    void shouldSendEmailSuccessfullyWhenEmailDataValid() {
        EmailData emailData = TestEmailData.getDefault();
        willDoNothing().given(javaMailSender).send(any(MimeMessage.class));

        emailService.sendEmail(EMAIL_FROM, emailData);
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void shouldSendEmailSuccessfullyWhenEmailWithoutAttachment() {
        EmailData emailData = TestEmailData.withoutAttachment();
        willDoNothing().given(javaMailSender).send(any(MimeMessage.class));

        emailService.sendEmail(EMAIL_FROM, emailData);
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void shouldThrowEmailFailedSendExceptionWhenMailExceptionOnSendEmail() {
        EmailData emailData = TestEmailData.getDefault();
        willThrow(mock(MailException.class)).given(javaMailSender).send(any(MimeMessage.class));

        assertThrows(EmailFailedSendException.class, () -> emailService.sendEmail(EMAIL_FROM, emailData));
    }

    @Test
    void shouldThrowInvalidArgumentExceptionWhenSendEmailWithNullSubject() {
        EmailData emailData = TestEmailData.getWithNullSubject();
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(EMAIL_FROM, emailData));
    }

    @Test
    void shouldThrowInvalidArgumentExceptionWhenSendEmailWithNullRecipient() {
        EmailData emailData = TestEmailData.getWithNullTo();
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(EMAIL_FROM, emailData));
    }

    @Test
    void shouldThrowInvalidArgumentExceptionWhenSendEmailWithNullMessageText() {
        EmailData emailData = TestEmailData.getWithNullMessageText();
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(EMAIL_FROM, emailData));
    }

    static class TestEmailData {
        static EmailData getDefault() {
            return EmailData.builder()
                .recipient(EMAIL_TO)
                .subject(EMAIL_SUBJECT)
                .message("")
                .attachments(of(json(EMAIL_ATTACHMENT_CONTENT, join(".", EMAIL_SUBJECT, "json"))))
                .build();
        }

        static EmailData getWithNullTo() {
            return EmailData.builder()
                .recipient(null)
                .subject(EMAIL_SUBJECT)
                .message("")
                .attachments(of(json(EMAIL_ATTACHMENT_CONTENT, join(".", EMAIL_SUBJECT, "json"))))
                .build();
        }

        static EmailData getWithNullSubject() {
            return EmailData.builder()
                .subject(null)
                .recipient(EMAIL_TO)
                .message("")
                .attachments(of(json(EMAIL_ATTACHMENT_CONTENT, join(".", EMAIL_SUBJECT, "json"))))
                .build();
        }

        static EmailData getWithNullMessageText() {
            return EmailData.builder()
                .subject(null)
                .recipient(EMAIL_TO)
                .message(null)
                .attachments(of(json(EMAIL_ATTACHMENT_CONTENT, join(".", EMAIL_SUBJECT, "json"))))
                .build();
        }

        static EmailData withoutAttachment() {
            return EmailData.builder()
                .recipient(EMAIL_TO)
                .subject(EMAIL_SUBJECT)
                .message("")
                .build();
        }
    }
}
