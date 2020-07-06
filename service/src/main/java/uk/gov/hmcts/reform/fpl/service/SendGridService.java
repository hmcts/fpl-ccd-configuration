package uk.gov.hmcts.reform.fpl.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.EmailFailedSendException;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;

import java.io.IOException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendGridService {
    private final SendGrid sendGrid;

    public void sendEmail(EmailData emailData) throws IOException {
        // Sender needs created and validated on SendGrid before use
        Email sender = new Email("jamesnelson117@gmail.com");
        // Recipient can be changed to receive emails to your own inbox
        Email recipient = new Email("James.Nelson@HMCTS.NET");
        Content content = new Content(MediaType.TEXT_PLAIN_VALUE, getMessage(emailData));

        Mail mail = new Mail(sender, emailData.getSubject(), recipient, content);

        if (emailData.hasAttachments()) {
            emailData.getAttachments().stream()
                .map(this::toSendGridAttachments)
                .forEach(mail::addAttachments);
        }

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }

    private String getMessage(EmailData emailData) {
        // SendGrid will not allow empty messages, but it's fine with blank messages.
        String message = emailData.getMessage();
        if (message == null || message.isBlank()) {
            message = " ";
        }
        return message;
    }

    private Attachments toSendGridAttachments(EmailAttachment attachment) {
        try {
            Attachments.Builder builder = new Attachments.Builder(
                attachment.getFilename(),
                attachment.getData().getInputStream()
            );
            builder.withType(attachment.getContentType());
            builder.withDisposition("attachment");
            return builder.build();
        } catch (IOException e) {
            throw new EmailFailedSendException(e);
        }
    }
}
