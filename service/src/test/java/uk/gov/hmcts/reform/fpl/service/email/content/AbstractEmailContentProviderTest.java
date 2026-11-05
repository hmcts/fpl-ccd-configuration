package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseUrlService.class, CourtService.class,
    DocumentService.class, LocalAuthorityChangedContentProvider.class})
@TestPropertySource(properties = "manage-case.ui.base.url:http://fake-url")
public abstract class AbstractEmailContentProviderTest {
    protected static final String LOCAL_AUTHORITY_NAME = LOCAL_AUTHORITY_1_NAME;
    protected static final String LOCAL_AUTHORITY_CODE = LOCAL_AUTHORITY_1_CODE;
    protected static final String CAFCASS_NAME = "cafcass";
    protected static final String CASE_REFERENCE = "12345";
    protected static final String UI_URL = "http://fake-url";
    protected static final String COURT_NAME = "Family Court";
    protected static final DocumentReference testDocument = DocumentReference.builder()
        .url("url")
        .binaryUrl("/testUrl")
        .build();
    protected static final String DOC_URL = "http://fake-url/testUrl";

    protected String caseUrl(String caseId) {
        return String.format("%s/cases/case-details/%s", UI_URL, caseId);
    }

    protected String caseUrl(String caseId, TabUrlAnchor tab) {
        return String.format("%s/cases/case-details/%s#%s", UI_URL, caseId, tab.getAnchor());
    }

    @MockitoBean
    protected DocumentDownloadService documentDownloadService;

    @MockitoBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockitoBean
    private UploadDocumentService uploadDocumentService;

    @MockitoBean
    protected FeatureToggleService featureToggleService;

    @MockitoBean
    protected CourtService courtService;

    @MockitoBean
    protected CaseUrlService caseUrlService;

    @MockitoBean
    protected DocumentService documentService;

    @MockitoBean
    protected EmailNotificationHelper notificationHelper;

    @BeforeEach
    private void init() {
        when(caseUrlService.getCaseUrl(ArgumentMatchers.anyLong(), any()))
            .thenAnswer(inv -> {
                Long caseId = inv.getArgument(0, Long.class);
                TabUrlAnchor tab = inv.getArgument(1, TabUrlAnchor.class);
                return caseUrl(caseId.toString(), tab);
            });
        when(caseUrlService.getCaseUrl(ArgumentMatchers.anyLong()))
            .thenAnswer(inv -> {
                Long caseId = inv.getArgument(0, Long.class);
                return caseUrl(caseId.toString());
            });
        when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(true);
        when(documentService.getDocumentBinaryUrl(any(DocumentReference.class)))
            .thenAnswer(inv -> ((DocumentReference) inv.getArgument(0)).getBinaryUrl());

        when(caseUrlService.getBaseUrl()).thenReturn("http://fake-url");
    }

}
