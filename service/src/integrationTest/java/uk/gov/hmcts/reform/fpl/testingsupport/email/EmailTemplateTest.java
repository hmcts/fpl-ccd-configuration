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
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.handlers.HmctsAdminNotificationHandler;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
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
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;

@SpringBootTest(classes = {ObjectMapper.class, NotificationService.class})
@ActiveProfiles({"integration-test", "email-template-test"})
@OverrideAutoConfiguration(enabled = true)
@Import(EmailTemplateTest.TestConfiguration.class)
//@Slf4j
public class EmailTemplateTest {

    @SpyBean
    private NotificationClient client;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @MockBean
    private HmctsAdminNotificationHandler hmctsAdminNotificationHandler;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    private ResultsCaptor<SendEmailResponse> resultsCaptor = new ResultsCaptor<>();

    @BeforeEach
    void setUp() throws NotificationClientException {
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn("File --- content --- pdf --- attachment".getBytes());
        doAnswer(resultsCaptor).when(client).sendEmail(any(), any(), any(), any());
    }

    @BeforeEach
    void lookupServiceSetUp() {
        when(inboxLookupService.getRecipients(any()))
            .thenReturn(Set.of("test@example.com"));
        when(hmctsCourtLookupConfiguration.getCourt(any()))
            .thenReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, "court@test.com", COURT_CODE));
        when(hmctsAdminNotificationHandler.getHmctsAdminEmail(any())).thenReturn("hmcts-admin@test.com");
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
