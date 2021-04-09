package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.Notification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UndeliveredEmailTest {

    @Mock
    private Notification notification;

    @Test
    void shouldThrowsExceptionWhenNotificationDoesNotHaveEmailAddress() {
        when(notification.getEmailAddress()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> UndeliveredEmail.fromNotification(notification))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot convert notification without email address into email");
    }

    @Test
    void shouldThrowsExceptionWhenNotificationDoesNotHaveSubject() {
        when(notification.getEmailAddress()).thenReturn(Optional.of("test@test.com"));
        when(notification.getSubject()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> UndeliveredEmail.fromNotification(notification))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot convert notification without subject into email");
    }

    @Test
    void shouldThrowsExceptionWhenNotificationDoesNotHaveReference() {
        when(notification.getEmailAddress()).thenReturn(Optional.of("test@test.com"));
        when(notification.getSubject()).thenReturn(Optional.of("Subject"));
        when(notification.getReference()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> UndeliveredEmail.fromNotification(notification))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot convert notification without reference into email");
    }

    @Test
    void shouldConvertNotificationToUndeliveredEmail() {
        when(notification.getEmailAddress()).thenReturn(Optional.of("test@test.com"));
        when(notification.getSubject()).thenReturn(Optional.of("Subject"));
        when(notification.getReference()).thenReturn(Optional.of("Reference"));

        UndeliveredEmail actualUndeliveredEmail = UndeliveredEmail.fromNotification(notification);
        UndeliveredEmail expectedUndeliveredEmail = UndeliveredEmail.builder()
            .recipient("test@test.com")
            .subject("Subject")
            .reference("Reference")
            .build();

        assertThat(actualUndeliveredEmail).isEqualTo(expectedUndeliveredEmail);
    }
}
