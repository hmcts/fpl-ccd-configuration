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
    void testFromFieldName() {
        assertThat(DocumentType.fromJsonFieldName("expertReportListCTSC")).isEqualTo(DocumentType.EXPERT_REPORTS);
        assertThat(DocumentType.fromJsonFieldName("courtBundleListV2")).isEqualTo(DocumentType.COURT_BUNDLE);
        assertThat(DocumentType.fromJsonFieldName("caseSummaryListLA")).isEqualTo(DocumentType.CASE_SUMMARY);
    }
}
