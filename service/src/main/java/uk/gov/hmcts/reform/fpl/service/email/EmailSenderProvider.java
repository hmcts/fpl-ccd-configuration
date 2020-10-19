package uk.gov.hmcts.reform.fpl.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderProvider {
    @Autowired
    @Qualifier("sendGridMailSender")
    private JavaMailSender sendGridMailSender;

    public JavaMailSender getMailSender() {
        return sendGridMailSender;
    }
}
