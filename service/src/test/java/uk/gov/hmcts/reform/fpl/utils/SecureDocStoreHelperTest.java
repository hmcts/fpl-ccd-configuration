package uk.gov.hmcts.reform.fpl.utils;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.exceptions.CaseProgressionReportException;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static java.lang.String.format;

@ExtendWith({SpringExtension.class, TestLogsExtension.class})
class SecureDocStoreHelperTest {

    @TestLogs
    private TestLogger logs = new TestLogger(SecureDocStoreHelper.class);
    @MockBean
    private FeatureToggleService featureToggleService;
    @Mock
    private SecureDocStoreService secureDocStoreService;
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String FILE_NAME = "FILE_NAME.pdf";

    @Nested
    class DocumentUpload {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnDocument(boolean toggleOn) {
            Document fromSecureDocStore = Document.builder().build();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE))
                .thenReturn(fromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                Document fromOldDmStoreApproach = Document.builder().build();
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE,
                    () -> fromOldDmStoreApproach);

                assertThat(actualDoc).isEqualTo(fromOldDmStoreApproach);
                assertThat(logs.getErrors()).isEmpty();
                assertThat(logs.getInfos())
                    .contains(
                        format("Using old dm-store approach to upload document: %s (%s).", FILE_NAME, CONTENT_TYPE));
            } else {
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE);
                assertThat(actualDoc).isEqualTo(fromSecureDocStore);
            }
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldLogExceptionWhenDocStoreApiFailure(boolean toggleOn) {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                Document fromOldDmStoreApproach = Document.builder().build();
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE,
                    () -> fromOldDmStoreApproach);

                assertThat(actualDoc).isEqualTo(fromOldDmStoreApproach);
                assertThat(logs.getErrorThrowableClassNames()).contains("java.lang.RuntimeException");
                assertThat(logs.getErrorThrowableMessages()).contains("TEST RUNTIME EXCEPTION");
                assertThat(logs.getErrors()).contains(
                    "↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN UPLOADING DOCUMENT (SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
                assertThat(logs.getInfos())
                    .contains(
                        format("Using old dm-store approach to upload document: %s (%s).", FILE_NAME, CONTENT_TYPE));
            } else {
                assertThatThrownBy(() -> underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE));
            }
        }
    }
}
