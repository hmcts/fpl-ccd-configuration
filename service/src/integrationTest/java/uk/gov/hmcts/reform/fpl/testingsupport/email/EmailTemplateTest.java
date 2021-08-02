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
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.utils.captor.ResultsCaptor;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;

@SpringBootTest(classes = {ObjectMapper.class, NotificationService.class})
@ActiveProfiles({"integration-test", "email-template-test"})
@OverrideAutoConfiguration(enabled = true)
@Import(EmailTemplateTest.TestConfiguration.class)
public class EmailTemplateTest {

    protected static final String CAFCASS_NAME = "cafcass";
    protected static final String CAFCASS_EMAIL = "cafcass@example.com";
    protected static final String GOV_NOTIFY_DOC_URL = "https://documents.service.gov.uk/d/";

    @SpyBean
    private NotificationClient client;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @MockBean
    private CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @MockBean
    private CourtService courtService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @MockBean
    private RepresentativesInbox inbox;

    private final ResultsCaptor<SendEmailResponse> resultsCaptor = new ResultsCaptor<>();

    @BeforeEach
    void notificationMocks() throws NotificationClientException {
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn("File --- content --- pdf --- attachment".getBytes());
        doAnswer(resultsCaptor).when(client).sendEmail(any(), any(), any(), any());
    }

    @BeforeEach
    void lookupServiceSetUp() {
        when(inbox.getEmailsByPreference(any(), any()))
            .thenReturn(new LinkedHashSet<>(Set.of("representative@example.com")));
        when(inboxLookupService.getRecipients(any())).thenReturn(Set.of("test@example.com"));
        when(courtService.getCourtEmail(any())).thenReturn(COURT_EMAIL_ADDRESS);
        when(courtService.getCourtName(any())).thenReturn(COURT_NAME);
        when(courtService.getCourtCode(any())).thenReturn(COURT_CODE);
        when(localAuthorityNameLookupConfiguration.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(cafcassLookupConfiguration.getCafcass(anyString()))
            .thenReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL));
        when(ctscTeamLeadLookupConfiguration.getEmail()).thenReturn("ctsc-team-lead@example.com");
    }

    protected String caseDetailsUrl(Long id) {
        return String.format("http://fake-url/cases/case-details/%s", id.toString());
    }

    protected String caseDetailsUrl(Long id, TabUrlAnchor tab) {
        return String.format("http://fake-url/cases/case-details/%s#%s", id.toString(), tab.getAnchor());
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

    protected List<SendEmailResponse> allResponses() {
        return resultsCaptor.getAllResults();
    }

}
