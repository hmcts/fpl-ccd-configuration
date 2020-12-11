package uk.gov.hmcts.reform.fpl.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.fpl.service.email.NotificationResponsePostProcessor;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.List;

@SpringBootTest(classes = {ObjectMapper.class, NotificationService.class})
@Import(EmailTemplateTest.TestConfiguration.class)
public class EmailTemplateTest {

    protected static final String NEW_LINE = "\r\n";

    @Autowired
    private RecordNotificationResponsePostProcessor notificationResponsePostProcessor;

    @Configuration
    public static class TestConfiguration {

        @Bean
        public NotificationClient notificationClient() {
            return new NotificationClient(
                "integrationtests-12f756df-f01d-4a32-a405-e1ea8a494fbb-0d14df98-a35d-4d56-9d0c-006094b18ed4");
        }

        @Bean
        public NotificationResponsePostProcessor notificationResponseRecorder() {
            return new RecordNotificationResponsePostProcessor();
        }

    }

    public static class RecordNotificationResponsePostProcessor implements NotificationResponsePostProcessor {

        private final List<SendEmailResponse> sentResponses = Lists.newArrayList();

        @Override
        public void process(SendEmailResponse response) {
            sentResponses.add(response);
        }

        public List<SendEmailResponse> getSentResponses() {
            return sentResponses;
        }
    }

    String line(String line) {
        return line + NEW_LINE;
    }

    String bodyFor(String x) {
        return notificationResponsePostProcessor.getSentResponses()
            .get(0)
            .getBody();
    }

}
