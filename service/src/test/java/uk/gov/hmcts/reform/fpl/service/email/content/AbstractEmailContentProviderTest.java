package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;

import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseUrlService.class})
@TestPropertySource(properties = "manage-case.ui.base.url:http://fake-url")
public abstract class AbstractEmailContentProviderTest {
    static final String LOCAL_AUTHORITY_NAME = LOCAL_AUTHORITY_1_NAME;
    static final String LOCAL_AUTHORITY_CODE = LOCAL_AUTHORITY_1_CODE;
    static final String CAFCASS_NAME = "cafcass";
    static final String CASE_REFERENCE = "12345";
    static final String UI_URL = "http://fake-url";
    static final String COURT_NAME = "Family Court";
    static final DocumentReference testDocument = DocumentReference.builder()
        .url("url")
        .binaryUrl("/testUrl")
        .build();
    static final String DOC_URL = "http://fake-url/testUrl";

    protected String caseUrl(String caseId) {
        return String.format("%s/cases/case-details/%s", UI_URL, caseId);
    }

    protected String caseUrl(String caseId, TabUrlAnchor tab) {
        return String.format("%s/cases/case-details/%s#%s", UI_URL, caseId, tab.getAnchor());
    }

    @MockBean
    protected DocumentDownloadService documentDownloadService;
}
