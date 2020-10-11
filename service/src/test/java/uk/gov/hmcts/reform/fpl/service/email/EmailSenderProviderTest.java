package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderProviderTest {

    @Mock(name = "mtaMailSender")
    private JavaMailSender mtaSender;

    @Mock(name = "sendGridMailSender")
    private JavaMailSender sendGridMailSender;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private EmailSenderProvider emailSenderProvider;

    @Test
    void shouldReturnSendGridMailSender() {
        when(featureToggleService.isSendGridEnabled()).thenReturn(true);

        assertThat(emailSenderProvider.getMailSender()).isEqualTo(sendGridMailSender);
    }

    @Test
    void shouldReturnMtaMailSender() {
        when(featureToggleService.isSendGridEnabled()).thenReturn(false);

        assertThat(emailSenderProvider.getMailSender()).isEqualTo(mtaSender);
    }

}
