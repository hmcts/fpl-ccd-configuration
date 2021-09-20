package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_SENT_FOR_TRANSLATION_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

class FurtherEvidenceDocumentsTransformerTest {

    private FurtherEvidenceDocumentsTransformer underTest = new FurtherEvidenceDocumentsTransformer();

    @Test
    void shouldReturnFurtherEvidenceDocumentBundleForTheFurtherEvidenceType() {
        List<Element<SupportingEvidenceBundle>> documentsBundles = List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT, ADMIN_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_TRANSLATED_DOCUMENT, ADMIN_SENT_FOR_TRANSLATION_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT, LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT
        );

        List<DocumentView> expectedDocumentsView = List.of(
            buildDocumentView(ADMIN_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(ADMIN_TRANSLATED_DOCUMENT.getValue()),
            buildDocumentView(ADMIN_SENT_FOR_TRANSLATION_DOCUMENT.getValue())
        );

        List<DocumentView> actual = underTest.getFurtherEvidenceDocumentsView(
            EXPERT_REPORTS, documentsBundles, true);

        assertThat(actual).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldReturnNonConfidentialFurtherEvidenceDocumentBundle() {
        List<Element<SupportingEvidenceBundle>> documentsBundles = List.of(
            LA_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT,
            ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT, LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT
        );

        List<DocumentView> expectedDocumentsView = List.of(
            buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())
        );

        List<DocumentView> actual = underTest.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, documentsBundles, false);

        assertThat(actual).isEqualTo(expectedDocumentsView);
    }

    @Test
    void shouldReturnEmptyListWhenAllDocumentsAreConfidential() {
        List<Element<SupportingEvidenceBundle>> documentsBundles = List.of(
            ADMIN_CONFIDENTIAL_DOCUMENT, LA_CONFIDENTIAL_DOCUMENT
        );

        List<DocumentView> actual = underTest.getFurtherEvidenceDocumentsView(
            GUARDIAN_REPORTS, documentsBundles, false);

        assertThat(actual).isEmpty();
    }

    private DocumentView buildDocumentView(SupportingEvidenceBundle document) {
        return DocumentView.builder()
            .document(document.getDocument())
            .fileName(document.getName())
            .type(document.getType().getLabel())
            .uploadedAt(formatLocalDateTimeBaseUsingFormat(document.getDateTimeUploaded(), TIME_DATE))
            .uploadedDateTime(document.getDateTimeUploaded())
            .uploadedBy(document.getUploadedBy())
            .documentName(document.getName())
            .translatedDocument(document.getTranslatedDocument())
            .sentForTranslation(document.sentForTranslation())
            .confidential(document.isConfidentialDocument())
            .title(document.getName())
            .build();
    }
}
