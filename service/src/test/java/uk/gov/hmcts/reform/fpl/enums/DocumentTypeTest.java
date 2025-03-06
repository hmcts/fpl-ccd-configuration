package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.ARCHIVED_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DRUG_AND_ALCOHOL_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.LETTER_OF_INSTRUCTION;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.POSITION_STATEMENTS_RESPONDENT;

class DocumentTypeTest {

    @Test
    void testGetCaseDataJsonPropertyNames() {
        assertThat(DocumentType.TOXICOLOGY_REPORT.getJsonFieldNames()).hasSize(3)
            .containsExactly("toxicologyStatementList", "toxicologyStatementListLA",
                "toxicologyStatementListCTSC");
        assertThat(DocumentType.COURT_BUNDLE.getJsonFieldNames()).hasSize(3)
            .containsExactly("courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC");
        assertThat(DocumentType.CASE_SUMMARY.getJsonFieldNames()).hasSize(3)
            .containsExactly("caseSummaryList", "caseSummaryListLA", "caseSummaryListCTSC");
        assertThat(DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS.getJsonFieldNames()).hasSize(0);
    }

    @Test
    void testFromJsonFieldName() {
        assertThat(DocumentType.fromJsonFieldName("toxicologyStatementListCTSC")).isEqualTo(
            DocumentType.TOXICOLOGY_REPORT);
        assertThat(DocumentType.fromJsonFieldName("courtBundleListV2")).isEqualTo(DocumentType.COURT_BUNDLE);
        assertThat(DocumentType.fromJsonFieldName("caseSummaryListLA")).isEqualTo(DocumentType.CASE_SUMMARY);
    }

    @Test
    void testFromFieldName() {
        assertThat(DocumentType.fromFieldName("PLACEMENT_RESPONSES")).isEqualTo(DocumentType.PLACEMENT_RESPONSES);
        assertThat(DocumentType.fromFieldName("hearingDocuments.caseSummaryList")).isEqualTo(DocumentType.CASE_SUMMARY);
        assertThat(DocumentType.fromFieldName("judgementList")).isEqualTo(DocumentType.JUDGEMENTS);
        assertThat(DocumentType.fromFieldName("hearingDocuments.courtBundleListV2"))
            .isEqualTo(DocumentType.COURT_BUNDLE);
    }

    @Test
    void testGetFieldNames() {
        assertThat(DocumentType.COURT_BUNDLE.getFieldNames()).hasSize(3)
            .containsExactly("hearingDocuments.courtBundleListV2", "hearingDocuments.courtBundleListLA",
                "hearingDocuments.courtBundleListCTSC");
        assertThat(DocumentType.JUDGEMENTS.getFieldNames()).hasSize(3)
            .containsExactly("judgementList", "judgementListLA","judgementListCTSC");
    }

    @Test
    void testGetFieldNameOfRemovedList() {
        assertThat(DocumentType.COURT_BUNDLE.getFieldNameOfRemovedList())
            .isEqualTo("hearingDocuments.courtBundleListRemoved");
        assertThat(DocumentType.CASE_SUMMARY.getFieldNameOfRemovedList())
            .isEqualTo("hearingDocuments.caseSummaryListRemoved");
        assertThat(DocumentType.JUDGEMENTS.getFieldNameOfRemovedList()).isEqualTo("judgementListRemoved");
    }

    @Test
    void testToJsonFieldName() {
        assertThat(DocumentType.toJsonFieldName("hearingDocuments.courtBundleListV2"))
            .isEqualTo("courtBundleListV2");
    }

    @Test
    void shouldHiddenFromUploadNewDocument() {
        assertThat(Arrays.stream(DocumentType.values()).filter(dt -> dt.isHiddenFromLAUpload()))
            .containsExactlyInAnyOrder(ARCHIVED_DOCUMENTS, POSITION_STATEMENTS_CHILD, POSITION_STATEMENTS_RESPONDENT,
                C1_APPLICATION_DOCUMENTS, C2_APPLICATION_DOCUMENTS, EXPERT_REPORTS, DRUG_AND_ALCOHOL_REPORTS,
                LETTER_OF_INSTRUCTION, GUARDIAN_REPORT);
        assertThat(Arrays.stream(DocumentType.values()).filter(dt -> dt.isHiddenFromCTSCUpload()))
            .containsExactlyInAnyOrder(ARCHIVED_DOCUMENTS, POSITION_STATEMENTS_CHILD, POSITION_STATEMENTS_RESPONDENT,
                C1_APPLICATION_DOCUMENTS, C2_APPLICATION_DOCUMENTS, EXPERT_REPORTS, DRUG_AND_ALCOHOL_REPORTS,
                LETTER_OF_INSTRUCTION);
        assertThat(Arrays.stream(DocumentType.values()).filter(dt -> dt.isHiddenFromSolicitorUpload()))
            .containsExactlyInAnyOrder(ARCHIVED_DOCUMENTS, POSITION_STATEMENTS_CHILD, POSITION_STATEMENTS_RESPONDENT,
                C1_APPLICATION_DOCUMENTS, C2_APPLICATION_DOCUMENTS, EXPERT_REPORTS, DRUG_AND_ALCOHOL_REPORTS,
                LETTER_OF_INSTRUCTION, GUARDIAN_REPORT);
    }
}
