package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DocumentTypeTest {

    @Test
    void testGetCaseDataJsonPropertyNames() {
        assertThat(DocumentType.EXPERT_REPORTS.getCaseDataJsonPropertyNames()).hasSize(3)
            .containsExactly("expertReportList", "expertReportListLA", "expertReportListCTSC");
        assertThat(DocumentType.COURT_BUNDLE.getCaseDataJsonPropertyNames()).hasSize(3)
            .containsExactly("courtBundleListV2", "courtBundleListLA", "courtBundleListCTSC");
        assertThat(DocumentType.CASE_SUMMARY.getCaseDataJsonPropertyNames()).hasSize(3)
            .containsExactly("caseSummaryList", "caseSummaryListLA", "caseSummaryListCTSC");
        assertThat(DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS.getCaseDataJsonPropertyNames()).hasSize(0);
    }
}
