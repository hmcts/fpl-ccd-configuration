package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
    private static final String DOCUMENT_URL_STRING = "http://localhost/test123";

    @Nested
    class DownloadDocument {

        @ParameterizedTest
        @ValueSource(booleans = { true, false})
        void shouldDownloadDocument(boolean toggleOn) {
            byte[] resultFromSecureDocStore = "DATA_FROM_NEW".getBytes();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.downloadDocument(DOCUMENT_URL_STRING)).thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                byte[] resultFromOldDmStoreApproach = "DATA_FROM_OLD".getBytes();
                byte[] actualData = underTest.download(DOCUMENT_URL_STRING, () -> resultFromOldDmStoreApproach);

                assertThat(actualData).isEqualTo(resultFromOldDmStoreApproach);
                assertThat(logs.getErrors()).isEmpty();
                assertThat(logs.getInfos()).contains(
                    format("Using old dm-store approach to download the document: %s.", DOCUMENT_URL_STRING));
                assertThat(logs.getInfos()).contains(
                    format("Downloaded document attempted from CDAM without error: %s", DOCUMENT_URL_STRING));
            } else {
                byte[] actualData = underTest.download(DOCUMENT_URL_STRING);
                assertThat(actualData).isEqualTo(resultFromSecureDocStore);
                assertThat(logs.getInfos()).containsExactly(format("Downloading document: {}", DOCUMENT_URL_STRING));
            }
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldLogExceptionWhenDocStoreApiFailure(boolean toggleOn) {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.downloadDocument(DOCUMENT_URL_STRING)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                byte[] resultFromOldDmStoreApproach = "DATA_FROM_OLD".getBytes();
                byte[] actualData = underTest.download(DOCUMENT_URL_STRING, () -> resultFromOldDmStoreApproach);

                assertThat(actualData).isEqualTo(resultFromOldDmStoreApproach);
                assertThat(logs.getErrorThrowableClassNames()).contains("java.lang.RuntimeException");
                assertThat(logs.getErrorThrowableMessages()).contains("TEST RUNTIME EXCEPTION");
                assertThat(logs.getErrors()).contains(
                    "↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT (SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
                assertThat(logs.getInfos())
                    .contains(
                        format("Using old dm-store approach to download the document: %s.", DOCUMENT_URL_STRING));
            } else {
                assertThatThrownBy(() -> underTest.download(DOCUMENT_URL_STRING))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }

    @Nested
    class GetDocumentMetadata {

        @ParameterizedTest
        @ValueSource(booleans = { false})
        void shouldDownloadDocument(boolean toggleOn) {
            Document resultFromSecureDocStore = Document.builder().build();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.getDocumentMetadata(DOCUMENT_URL_STRING)).thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                DocumentReference resultFromOldDmStoreApproach = DocumentReference.builder().build();
                DocumentReference actualMetadata = underTest.getDocumentMetadata(DOCUMENT_URL_STRING,
                    () -> resultFromOldDmStoreApproach);

                assertThat(actualMetadata).isEqualTo(resultFromOldDmStoreApproach);
                assertThat(logs.getErrors()).isEmpty();
                assertThat(logs.getInfos()).contains(
                    format("Downloaded document meta data attempted from CDAM without error: %s", DOCUMENT_URL_STRING));
                assertThat(logs.getInfos()).contains(
                    format("Using old dm-store approach to download document meta data: %s.", DOCUMENT_URL_STRING));
            } else {
                DocumentReference actualMetadata = underTest.getDocumentMetadata(DOCUMENT_URL_STRING);
                assertThat(actualMetadata).isEqualTo(SecureDocStoreHelper
                    .convertToDocumentReference(DOCUMENT_URL_STRING, resultFromSecureDocStore));
                assertThat(logs.getInfos()).containsExactly(format("Downloading document meta data: {}",
                    DOCUMENT_URL_STRING));
            }
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldLogExceptionWhenDocStoreApiFailure(boolean toggleOn) {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.getDocumentMetadata(DOCUMENT_URL_STRING)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                DocumentReference resultFromOldDmStoreApproach = DocumentReference.builder().build();
                DocumentReference actualMetadata = underTest.getDocumentMetadata(DOCUMENT_URL_STRING,
                    () -> resultFromOldDmStoreApproach);

                assertThat(actualMetadata).isEqualTo(resultFromOldDmStoreApproach);
                assertThat(logs.getErrorThrowableClassNames()).contains("java.lang.RuntimeException");
                assertThat(logs.getErrorThrowableMessages()).contains("TEST RUNTIME EXCEPTION");
                assertThat(logs.getErrors()).contains(
                    "↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN DOWNLOADING METADATA "
                        + "(SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
                assertThat(logs.getInfos())
                    .contains(
                        format("Using old dm-store approach to download document meta data: %s.", DOCUMENT_URL_STRING));
            } else {
                assertThatThrownBy(() -> underTest.getDocumentMetadata(DOCUMENT_URL_STRING))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }

    @Nested
    class UploadDocument {

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
                assertThat(logs.getInfos()).contains(
                    format("Uploaded document attempted from CDAM without error: %s (%s)", FILE_NAME, CONTENT_TYPE));
            } else {
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE);
                assertThat(actualDoc).isEqualTo(fromSecureDocStore);
                assertThat(logs.getInfos()).containsExactly(format("Uploading document file name: %s (%s)",
                    FILE_NAME, CONTENT_TYPE));
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
                assertThat(logs.getErrors()).contains("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN UPLOADING DOCUMENT "
                    + "(SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
                assertThat(logs.getInfos())
                    .contains(
                        format("Using old dm-store approach to upload document: %s (%s).", FILE_NAME, CONTENT_TYPE));
            } else {
                assertThatThrownBy(() -> underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }
}
