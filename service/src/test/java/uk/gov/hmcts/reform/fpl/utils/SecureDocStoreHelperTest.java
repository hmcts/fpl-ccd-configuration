package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

        @Test
        void shouldThrownUnsupportedOperationExceptionIfOldApproachNotProvidedWhenToggledOff() {
            byte[] resultFromSecureDocStore = "DATA_FROM_NEW".getBytes();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(false);
            when(secureDocStoreService.downloadDocument(DOCUMENT_URL_STRING)).thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            assertThatThrownBy(() -> underTest.download(DOCUMENT_URL_STRING))
                .isInstanceOf(UnsupportedOperationException.class);
            assertThat(logs.getErrors()).isEmpty();
        }

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
            } else {
                byte[] actualData = underTest.download(DOCUMENT_URL_STRING);
                assertThat(actualData).isEqualTo(resultFromSecureDocStore);
                assertThat(logs.getInfos()).containsExactly(format("Downloading document: %s", DOCUMENT_URL_STRING));
            }
        }

        @Test
        void shouldThrowExceptionWhenToggledOn() {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(true);
            when(secureDocStoreService.downloadDocument(DOCUMENT_URL_STRING)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);
            assertThatThrownBy(() -> underTest.download(DOCUMENT_URL_STRING))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("TEST RUNTIME EXCEPTION");
            assertThat(logs.getErrors()).doesNotContain(
                "↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT (SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
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
            } else {
                assertThatThrownBy(() -> underTest.download(DOCUMENT_URL_STRING))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }

    @Nested
    class GetDocumentMetadata {

        @Test
        void shouldThrownUnsupportedOperationExceptionIfOldApproachNotProvidedWhenToggledOff() {
            Document resultFromSecureDocStore = Document.builder().build();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(false);
            when(secureDocStoreService.getDocumentMetadata(DOCUMENT_URL_STRING)).thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            assertThatThrownBy(() -> underTest.getDocumentMetadata(DOCUMENT_URL_STRING))
                .isInstanceOf(UnsupportedOperationException.class);
            assertThat(logs.getErrors()).isEmpty();
        }

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
            } else {
                DocumentReference actualMetadata = underTest.getDocumentMetadata(DOCUMENT_URL_STRING);
                assertThat(actualMetadata).isEqualTo(SecureDocStoreHelper
                    .convertToDocumentReference(DOCUMENT_URL_STRING, resultFromSecureDocStore));
                assertThat(logs.getInfos()).containsExactly(format("Downloading document meta data: {}",
                    DOCUMENT_URL_STRING));
            }
        }

        @Test
        void shouldThrowExceptionWhenToggledOn() {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(true);
            when(secureDocStoreService.getDocumentMetadata(DOCUMENT_URL_STRING)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);
            assertThatThrownBy(() -> underTest.getDocumentMetadata(DOCUMENT_URL_STRING))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("TEST RUNTIME EXCEPTION");
            assertThat(logs.getErrors()).doesNotContain(
                "↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN DOWNLOADING METADATA "
                    + "(SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
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
            } else {
                assertThatThrownBy(() -> underTest.getDocumentMetadata(DOCUMENT_URL_STRING))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }

    @Nested
    class UploadDocument {

        @Test
        void shouldThrownUnsupportedOperationExceptionIfOldApproachNotProvidedWhenToggledOff() {
            Document resultFromSecureDocStore = Document.builder().build();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(false);
            when(secureDocStoreService.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE))
                .thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            byte[] data = "DATA".getBytes();
            assertThatThrownBy(() -> underTest.uploadDocument(data, FILE_NAME, CONTENT_TYPE))
                .isInstanceOf(UnsupportedOperationException.class);
            assertThat(logs.getErrors()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnDocument(boolean toggleOn) {
            Document resultFromSecureDocStore = Document.builder().build();
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(toggleOn);
            when(secureDocStoreService.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE))
                .thenReturn(resultFromSecureDocStore);

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);

            if (toggleOn == false) {
                Document fromOldDmStoreApproach = Document.builder().build();
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE,
                    () -> fromOldDmStoreApproach);

                assertThat(actualDoc).isEqualTo(fromOldDmStoreApproach);
                assertThat(logs.getErrors()).isEmpty();
            } else {
                Document actualDoc = underTest.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE);
                assertThat(actualDoc).isEqualTo(resultFromSecureDocStore);
                assertThat(logs.getInfos()).containsExactly(format("Uploading document file name: %s (%s)",
                    FILE_NAME, CONTENT_TYPE));
            }
        }

        @Test
        void shouldThrowExceptionWhenToggledOn() {
            when(featureToggleService.isSecureDocstoreEnabled()).thenReturn(true);
            when(secureDocStoreService.uploadDocument("DATA".getBytes(), FILE_NAME, CONTENT_TYPE)).thenThrow(
                new RuntimeException("TEST RUNTIME EXCEPTION"));

            SecureDocStoreHelper underTest = new SecureDocStoreHelper(secureDocStoreService, featureToggleService);
            byte[] data = "DATA".getBytes();
            assertThatThrownBy(() -> underTest.uploadDocument(data, FILE_NAME, CONTENT_TYPE))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("TEST RUNTIME EXCEPTION");
            assertThat(logs.getErrors()).doesNotContain("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN UPLOADING DOCUMENT "
                + "(SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑");
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
            } else {
                byte[] data = "DATA".getBytes();
                assertThatThrownBy(() -> underTest.uploadDocument(data, FILE_NAME, CONTENT_TYPE))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("TEST RUNTIME EXCEPTION");
            }
        }
    }
}
