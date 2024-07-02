package uk.gov.hmcts.reform.fpl.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(final String from, final EmailData emailData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

            mimeMessageHelper.setTo(emailData.getRecipient());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setSubject(emailData.getSubject());
            mimeMessageHelper.setText(emailData.getMessage());

            if (emailData.hasAttachments()) {
                // preferring for loop here so we don't have to catch exceptions twice
                for (EmailAttachment attachment : emailData.getAttachments()) {
                    mimeMessageHelper.addAttachment(attachment.getFilename(),
                        attachment.getData(), attachment.getContentType());
                }
            }

            mailSender.send(message);

        } catch (MessagingException | MailException e) {
            throw new EmailFailedSendException(e);
        }
    }
}
