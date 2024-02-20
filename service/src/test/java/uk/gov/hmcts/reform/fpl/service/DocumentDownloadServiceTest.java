package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.document.domain.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class DocumentDownloadServiceTest {

    @Mock
    private SecureDocStoreService secureDocStoreService;

    @Mock
    private FeatureToggleService featureToggleService;

    private DocumentDownloadService documentDownloadService;

    private final Document document = document();

    @BeforeEach
    void setup() {
        given(featureToggleService.isSecureDocstoreEnabled()).willReturn(false);

        documentDownloadService = new DocumentDownloadService(secureDocStoreService,
            featureToggleService);
    }

    @Test
    void shouldDownloadDocumentUsingCDAMApproach() {
        Document document = document();
        byte[] expectedDocumentContents = "test".getBytes();

        given(secureDocStoreService.downloadDocument(anyString()))
            .willReturn(expectedDocumentContents);

        byte[] documentContents = documentDownloadService.downloadDocument(document.links.binary.href);

        assertThat(documentContents).isEqualTo(expectedDocumentContents);
    }

    @Test
    void shouldThrowExceptionWhenDownloadBinaryReturnsNull() {
        given(secureDocStoreService.downloadDocument(anyString()))
            .willReturn(null);

        UnsupportedOperationException thrownException = assertThrows(UnsupportedOperationException.class,
            () -> documentDownloadService.downloadDocument(document.links.binary.href));

        assertThat(thrownException.getMessage()).contains("unsuccessful");
    }
}
