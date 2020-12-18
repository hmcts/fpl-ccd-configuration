package uk.gov.hmcts.reform.fpl.testingsupport.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.utils.captor.ResultsCaptor;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ObjectMapper.class, NotificationService.class})
@ActiveProfiles( {"integration-test", "email-template-test"})
@OverrideAutoConfiguration(enabled = true)
@Import(EmailTemplateTest.TestConfiguration.class)
public class EmailTemplateTest {

    @SpyBean
    private NotificationClient client;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private InboxLookupService inboxLookupService;

    private ResultsCaptor<SendEmailResponse> resultsCaptor = new ResultsCaptor<>();

    @BeforeEach
    void setUp() throws NotificationClientException {
        when(inboxLookupService.getRecipients(any()))
            .thenReturn(Set.of("test@example.com"));
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn("File --- content --- pdf --- attachment".getBytes());
        doAnswer(resultsCaptor).when(client).sendEmail(any(), any(), any(), any());
    }

    public static class TestConfiguration {
        @Bean
        public NotificationClient notificationClient(@Value("${integration-test.notify-service.key}") String key) {
            return new NotificationClient(key);
        }
    }

    protected SendEmailResponse response() {
        return resultsCaptor.getResult();
    }
}
