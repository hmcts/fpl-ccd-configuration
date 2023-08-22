package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DocumentTypeTest {

    @Test
    void testGetCaseDataJsonPropertyNames() {
        assertThat(DocumentType.EXPERT_REPORTS.getJsonFieldNames()).hasSize(3)
            .containsExactly("expertReportList", "expertReportListLA", "expertReportListCTSC");
        assertThat(DocumentType.COURT_BUNDLE.getJsonFieldNames()).hasSize(3)
            .containsExactly("courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC");
        assertThat(DocumentType.CASE_SUMMARY.getJsonFieldNames()).hasSize(3)
            .containsExactly("caseSummaryList", "caseSummaryListLA", "caseSummaryListCTSC");
        assertThat(DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS.getJsonFieldNames()).hasSize(0);
    }

    @Test
    void testFromJsonFieldName() {
        assertThat(DocumentType.fromJsonFieldName("expertReportListCTSC")).isEqualTo(DocumentType.EXPERT_REPORTS);
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
}
