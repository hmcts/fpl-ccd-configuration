package uk.gov.hmcts.reform.fpl.config.robotics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RoboticsEmailConfigurationTest {
    private static final String EXPECTED_NOTIFICATIONS_SENDER = "sender@example.com";
    private static final String EXPECTED_NOTIFICATIONS_RECIPIENT = "recipient@example.com";

    private RoboticsEmailConfiguration roboticsEmailConfiguration;

    @Test
    void shouldReturnNullWhenInstantiatedWithDefaultConstructorForSenderAndRecipient() {
        roboticsEmailConfiguration = new RoboticsEmailConfiguration();

        assertThat(roboticsEmailConfiguration.getRecipient()).isNull();
        assertThat(roboticsEmailConfiguration.getSender()).isNull();
    }

    @Test
    void shouldReturnPassedInSenderAndRecipientWhenInstantiatedWithAllArgsConstructor() {
        roboticsEmailConfiguration = new RoboticsEmailConfiguration(EXPECTED_NOTIFICATIONS_SENDER,
            EXPECTED_NOTIFICATIONS_RECIPIENT);

        assertThat(roboticsEmailConfiguration.getSender()).isEqualTo(EXPECTED_NOTIFICATIONS_SENDER);
        assertThat(roboticsEmailConfiguration.getRecipient()).isEqualTo(EXPECTED_NOTIFICATIONS_RECIPIENT);
    }
}
