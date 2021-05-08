package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class HearingBundleTransformerTest {

    @Mock
    private FurtherEvidenceDocumentsTransformer furtherEvidenceTransformer;

    @InjectMocks
    private HearingBundleTransformer underTest;

    @Captor
    private ArgumentCaptor<List<Element<SupportingEvidenceBundle>>> furtherEvidenceCaptor;

    private final List<Element<HearingFurtherEvidenceBundle>> hearingEvidenceDocuments
        = buildHearingFurtherEvidenceDocuments(UUID.randomUUID());

    @Test
    void shouldReturnAllDocumentsForHmctsView() {
        List<Element<SupportingEvidenceBundle>> expectedHearingEvidenceDocuments = List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_DOCUMENT,
            LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(expectedHearingEvidenceDocuments))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        underTest.getHearingBundleView(hearingEvidenceDocuments, DocumentViewType.HMCTS);

        verify(furtherEvidenceTransformer).getFurtherEvidenceDocumentBundles(furtherEvidenceCaptor.capture());
        assertThat(furtherEvidenceCaptor.getValue()).isEqualTo(expectedHearingEvidenceDocuments);
    }

    @Test
    void shouldReturnAllDocumentsForLAView() {
        List<Element<SupportingEvidenceBundle>> expectedHearingEvidenceDocuments = List.of(
            ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_DOCUMENT,
            LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(expectedHearingEvidenceDocuments))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        underTest.getHearingBundleView(hearingEvidenceDocuments, DocumentViewType.LA);

        verify(furtherEvidenceTransformer).getFurtherEvidenceDocumentBundles(furtherEvidenceCaptor.capture());
        assertThat(furtherEvidenceCaptor.getValue()).isEqualTo(expectedHearingEvidenceDocuments);
    }

    @Test
    void shouldReturnAllDocumentsForNonConfidentialView() {
        List<Element<SupportingEvidenceBundle>> expectedHearingEvidenceDocuments = List.of(
            ADMIN_NON_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);

        when(furtherEvidenceTransformer.getFurtherEvidenceDocumentBundles(expectedHearingEvidenceDocuments))
            .thenReturn(List.of(DocumentBundleView.builder().build()));

        underTest.getHearingBundleView(hearingEvidenceDocuments, DocumentViewType.NONCONFIDENTIAL);

        verify(furtherEvidenceTransformer).getFurtherEvidenceDocumentBundles(furtherEvidenceCaptor.capture());
        assertThat(furtherEvidenceCaptor.getValue()).isEqualTo(expectedHearingEvidenceDocuments);
    }

    private List<Element<HearingFurtherEvidenceBundle>> buildHearingFurtherEvidenceDocuments(UUID hearingId) {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT);

        return List.of(element(hearingId, HearingFurtherEvidenceBundle.builder()
            .supportingEvidenceBundle(furtherEvidenceBundle)
            .build()));
    }

}
