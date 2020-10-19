package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmailSenderProviderTest {
    @Mock(name = "sendGridMailSender")
    private JavaMailSender sendGridMailSender;

    @InjectMocks
    private EmailSenderProvider emailSenderProvider;

    @Test
    void shouldReturnSendGridMailSender() {
        assertThat(emailSenderProvider.getMailSender()).isEqualTo(sendGridMailSender);
    }
}
