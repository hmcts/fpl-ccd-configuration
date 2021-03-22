package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.hmcts.reform.fpl.model.notify.UndeliveredEmailsNotifyData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndeliveredEmailsContentProviderTest {

    private final UndeliveredEmailsContentProvider underTest = new UndeliveredEmailsContentProvider();

    @Test
    void shouldBuildNotifyDataForUndeliveredEmailWithReference() {
        UndeliveredEmail undeliveredEmail = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference("test1")
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest.buildParameters(List.of(undeliveredEmail));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1\n"
                + "Reference: test1");
    }

    @Test
    void shouldBuildNotifyDataForUndeliveredEmailWithEnvPrefixedReference() {
        UndeliveredEmail undeliveredEmail = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference("prod/test1")
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest.buildParameters(List.of(undeliveredEmail));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1\n"
                + "Reference: test1");
    }

    @Test
    void shouldBuildNotifyDataForUndeliveredEmailWithCaseIdAsReference() {
        UndeliveredEmail undeliveredEmail = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference("1234567898765432")
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest.buildParameters(List.of(undeliveredEmail));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1\n"
                + "Case id: 1234567898765432");
    }

    @Test
    void shouldBuildNotifyDataForUndeliveredEmailWithEnvPrefixedCaseIdAsReference() {
        UndeliveredEmail undeliveredEmail = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference("prod/1234567898765432")
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest.buildParameters(List.of(undeliveredEmail));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1\n"
                + "Case id: 1234567898765432");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldBuildNotifyDataForUndeliveredEmailWithoutReference(String reference) {
        UndeliveredEmail undeliveredEmail = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference(reference)
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest.buildParameters(List.of(undeliveredEmail));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1");
    }

    @Test
    void shouldBuildNotifyDataForMultipleUndeliveredEmails() {
        UndeliveredEmail undeliveredEmail1 = UndeliveredEmail.builder()
            .recipient("test1@test.com")
            .subject("Test subject 1")
            .reference("prod/test1")
            .build();

        UndeliveredEmail undeliveredEmail2 = UndeliveredEmail.builder()
            .recipient("test2@test.com")
            .subject("Test subject 2")
            .reference("1234567898765432")
            .build();

        UndeliveredEmailsNotifyData notifyData = underTest
            .buildParameters(List.of(undeliveredEmail1, undeliveredEmail2));

        assertThat(notifyData.getEmails()).isEqualTo(
            "To: test1@test.com\n"
                + "Subject: Test subject 1\n"
                + "Reference: test1\n"
                + "\n"
                + "To: test2@test.com\n"
                + "Subject: Test subject 2\n"
                + "Case id: 1234567898765432");
    }
}
