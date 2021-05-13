package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.RESPONDENT1;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.RESPONDENT2;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.SUPPORTING_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class RespondentStatementsTransformerTest {

    private static final String RESPONDENT_STATEMENT_TYPE = "Respondent statements";

    private RespondentStatementsTransformer underTest = new RespondentStatementsTransformer();

    @Test
    void shouldReturnRespondentStatementsForHmctsView() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(RESPONDENT1, RESPONDENT2))
            .respondentStatements(wrapElements(
                RespondentStatement.builder()
                    .respondentId(RESPONDENT1.getId())
                    .respondentName(RESPONDENT1.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(SUPPORTING_EVIDENCE_DOCUMENTS)
                    .build()))
            .build();

        List<DocumentView> expectedDocuments = List.of(
            buildDocumentView(ADMIN_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(LA_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue()));

        List<DocumentBundleView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.HMCTS);

        assertThat(documentBundleView).isEqualTo(List.of(
            DocumentBundleView.builder().name("Respondent 1 statements").documents(expectedDocuments).build()));
    }

    @Test
    void shouldReturnRespondentStatementsForLAView() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(RESPONDENT1, RESPONDENT2))
            .respondentStatements(wrapElements(
                RespondentStatement.builder()
                    .respondentId(RESPONDENT2.getId())
                    .respondentName(RESPONDENT2.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(SUPPORTING_EVIDENCE_DOCUMENTS)
                    .build()))
            .build();

        List<DocumentView> expectedDocuments = List.of(
            buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(LA_CONFIDENTIAL_DOCUMENT.getValue()),
            buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue()));

        List<DocumentBundleView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.LA);

        assertThat(documentBundleView).isEqualTo(List.of(
            DocumentBundleView.builder().name("Respondent 2 statements").documents(expectedDocuments).build()));
    }

    @Test
    void shouldReturnRespondentStatementsForNonConfidentialViewAndSortByUploadedDate() {
        Element<SupportingEvidenceBundle> respondent2DocWithoutDate = element(SupportingEvidenceBundle.builder()
            .name("respondent2 doc1").document(testDocumentReference())
            .type(FurtherEvidenceType.APPLICANT_STATEMENT)
            .uploadedBy(null).dateTimeUploaded(null)
            .build());
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(RESPONDENT1, RESPONDENT2))
            .respondentStatements(wrapElements(
                RespondentStatement.builder()
                    .respondentId(RESPONDENT1.getId())
                    .respondentName(RESPONDENT1.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(List.of(ADMIN_CONFIDENTIAL_DOCUMENT, LA_NON_CONFIDENTIAL_DOCUMENT))
                    .build(),
                RespondentStatement.builder()
                    .respondentId(RESPONDENT2.getId())
                    .respondentName(RESPONDENT2.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(List.of(
                        respondent2DocWithoutDate, ADMIN_NON_CONFIDENTIAL_DOCUMENT, LA_CONFIDENTIAL_DOCUMENT))
                    .build()))
            .build();

        List<DocumentBundleView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.NONCONFIDENTIAL);

        DocumentBundleView respondent1Bundle = DocumentBundleView.builder()
            .name("Respondent 1 statements")
            .documents(List.of(buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())))
            .build();

        DocumentBundleView respondent2Bundle = DocumentBundleView.builder()
            .name("Respondent 2 statements")
            .documents(List.of(buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue()),
                buildDocumentView(respondent2DocWithoutDate.getValue())))
            .build();

        assertThat(documentBundleView).isEqualTo(List.of(respondent1Bundle, respondent2Bundle));
    }

    @Test
    void shouldReturnEmptyBundleWhenNoRespondentStatementsExistForNonConfidentialView() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(RESPONDENT1, RESPONDENT2))
            .respondentStatements(wrapElements(
                RespondentStatement.builder()
                    .respondentId(RESPONDENT1.getId())
                    .respondentName(RESPONDENT1.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(List.of(ADMIN_CONFIDENTIAL_DOCUMENT, LA_CONFIDENTIAL_DOCUMENT))
                    .build()))
            .build();

        List<DocumentBundleView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.NONCONFIDENTIAL);

        assertThat(documentBundleView).isEqualTo(List.of());
    }

    private DocumentView buildDocumentView(SupportingEvidenceBundle document) {
        return DocumentView.builder()
            .document(document.getDocument())
            .fileName(document.getName())
            .type(RESPONDENT_STATEMENT_TYPE)
            .uploadedAt(isNotEmpty(document.getDateTimeUploaded())
                ? formatLocalDateTimeBaseUsingFormat(document.getDateTimeUploaded(), TIME_DATE) : null)
            .uploadedBy(document.getUploadedBy())
            .documentName(document.getName())
            .confidential(document.isConfidentialDocument())
            .title(document.getName())
            .build();
    }
}
