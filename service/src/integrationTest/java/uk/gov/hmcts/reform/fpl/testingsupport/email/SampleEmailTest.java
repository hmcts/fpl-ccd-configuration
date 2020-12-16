package uk.gov.hmcts.reform.fpl.testingsupport.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.notify.TestNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;

import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;

public class SampleEmailTest extends EmailTemplateTest {

    private static final String TEST_TEMPLATE_ID = "b7beba34-95e8-4619-9476-d16d60c9706b";

    @Autowired
    NotificationService underTest;

    @Test
    void testEmail() {
        underTest.sendEmail(
            TEST_TEMPLATE_ID,
            "test@example.com",
            TestNotifyData.builder()
                .fieldB("microsoft-edge:https://www.google.com")
                .fieldA("https://www.google.com")
                .build(),
            "testCaseID" + UUID.randomUUID());


        assertThat(response()).hasBody(emailContent()
            .line("# This is a title")
            .line()
            .line("Apply now (normal link) at https://www.google.com ")
            .line("Apply now (edge link) at microsoft-edge:https://www.google.com ")
            .line()
            .line("Thanks,")
            .end("Test")
        );

    }
}
