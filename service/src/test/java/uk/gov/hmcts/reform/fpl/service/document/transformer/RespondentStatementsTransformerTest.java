package uk.gov.hmcts.reform.fpl.service.document.transformer;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentBundleView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentContainerView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentView;
import uk.gov.hmcts.reform.fpl.model.documentview.DocumentViewType;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.ADMIN_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.LA_NON_CONFIDENTIAL_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.RESPONDENT1;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.RESPONDENT2;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.SUPPORTING_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.service.document.transformer.DocumentViewTestHelper.buildFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RespondentStatementsTransformerTest {

    private static final String RESPONDENT_STATEMENT_TYPE = "Respondent statements";

    private RespondentStatementsTransformer underTest = new RespondentStatementsTransformer();

    @Test
    void shouldReturnRespondentStatementsForHmctsViewAndSortByUploadedDateTime() {
        Element<SupportingEvidenceBundle> respondent1Document1 = buildFurtherEvidenceBundle(
            "Admin uploaded evidence1", "HMCTS", true, EXPERT_REPORTS,
            LocalDateTime.of(2021, 5, 13, 20, 55, 0), null, null);

        Element<SupportingEvidenceBundle> respondent1Document2 = buildFurtherEvidenceBundle(
            "Admin uploaded evidence1", "HMCTS", true, EXPERT_REPORTS,
            LocalDateTime.of(2021, 5, 13, 22, 13, 0), null, null);

        Element<SupportingEvidenceBundle> respondent1Document3WithoutDate = buildFurtherEvidenceBundle(
            "Admin uploaded evidence1", "HMCTS", true, EXPERT_REPORTS, null, null, null);

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(RESPONDENT1, RESPONDENT2))
            .respondentStatements(wrapElements(
                RespondentStatement.builder()
                    .respondentId(RESPONDENT1.getId())
                    .respondentName(RESPONDENT1.getValue().getParty().getFullName())
                    .supportingEvidenceBundle(List.of(
                        respondent1Document1, respondent1Document2, respondent1Document3WithoutDate))
                    .build()))
            .build();

        List<DocumentView> expectedDocuments = List.of(
            buildDocumentView(respondent1Document2.getValue()),
            buildDocumentView(respondent1Document1.getValue()),
            buildDocumentView(respondent1Document3WithoutDate.getValue()));

        List<DocumentContainerView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.HMCTS);

        assertThat(documentBundleView).isEqualTo(List.of(
            DocumentBundleView.builder()
                .name("Dave Miller statements")
                .documents(expectedDocuments)
                .build())
        );
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

        List<DocumentContainerView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.LA);

        assertThat(documentBundleView).isEqualTo(List.of(
            DocumentBundleView.builder()
                .name("Will Smith statements")
                .documents(expectedDocuments)
                .build())
        );
    }

    @Test
    void shouldReturnRespondentStatementsForNonConfidentialViewAndSortByUploadedDate() {
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
                    .respondentName("Will Smith")
                    .supportingEvidenceBundle(List.of(ADMIN_NON_CONFIDENTIAL_DOCUMENT, LA_CONFIDENTIAL_DOCUMENT))
                    .build()))
            .build();

        List<DocumentContainerView> documentBundleView = underTest.getRespondentStatementsBundle(
            caseData, DocumentViewType.NONCONFIDENTIAL);

        DocumentBundleView respondent1Bundle = DocumentBundleView.builder()
            .name(RESPONDENT1.getValue().getParty().getFullName() + " statements")
            .documents(List.of(buildDocumentView(LA_NON_CONFIDENTIAL_DOCUMENT.getValue())))
            .build();

        DocumentBundleView respondent2Bundle = DocumentBundleView.builder()
            .name(RESPONDENT2.getValue().getParty().getFullName() + " statements")
            .documents(List.of(buildDocumentView(ADMIN_NON_CONFIDENTIAL_DOCUMENT.getValue())))
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
                    .respondentName("Dave Miller")
                    .supportingEvidenceBundle(List.of(ADMIN_CONFIDENTIAL_DOCUMENT, LA_CONFIDENTIAL_DOCUMENT))
                    .build()))
            .build();

        List<DocumentContainerView> documentBundleView = underTest.getRespondentStatementsBundle(
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
