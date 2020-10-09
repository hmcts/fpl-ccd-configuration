package uk.gov.hmcts.reform.fpl.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

@Service
public class EmailSenderProvider {

    @Autowired
    private FeatureToggleService featureToggleService;

    @Autowired
    @Qualifier("mtaMailSender")
    private JavaMailSender mtaMailSender;

    @Autowired
    @Qualifier("sendGridMailSender")
    private JavaMailSender sendGridMailSender;

    public JavaMailSender getMailSender() {
        return featureToggleService.isSendGridEnabled() ? sendGridMailSender : mtaMailSender;
    }

}
