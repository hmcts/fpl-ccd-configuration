package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public abstract class AbstractEmailContentProviderTest {
    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    static final String LOCAL_AUTHORITY_CODE = "example";
    static final String CAFCASS_NAME = "cafcass";
    static final String CASE_REFERENCE = "12345";
    static final String UI_URL = "http://fake-url";
    static final String COURT_NAME = "Family Court";

    protected String caseUrl(String caseId) {
        return formatCaseUrl(UI_URL, Long.valueOf(caseId));
    }

    protected String caseUrl(String caseId, String tab) {
        return formatCaseUrl(UI_URL, Long.valueOf(caseId), tab);
    }

    @MockBean
    CaseUrlService caseUrlService;

    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    DocumentDownloadService documentDownloadService;

    @BeforeEach
    void initCaseUrlService() {
        when(caseUrlService.getCaseUrl(anyLong()))
            .thenAnswer(invocation -> caseUrl(invocation.getArgument(0).toString()));

        when(caseUrlService.getCaseUrl(anyLong(), anyString()))
            .thenAnswer(invocation -> caseUrl(invocation.getArgument(0).toString(), invocation.getArgument(1)));

        when(caseUrlService.getBaseUrl())
            .thenAnswer(invocation -> UI_URL);
    }
}
