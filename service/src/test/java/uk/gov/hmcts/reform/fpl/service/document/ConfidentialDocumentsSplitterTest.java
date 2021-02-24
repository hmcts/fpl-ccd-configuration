package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ConfidentialDocumentsSplitterTest {

    private static final SupportingEvidenceBundle CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .name("Confidential doc")
        .confidential(List.of("CONFIDENTIAL"))
        .build();

    private static final SupportingEvidenceBundle NON_CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .name("Non confidential doc")
        .confidential(null)
        .build();

    private static final String KEY = "test";
    private static final String NON_CONFIDENTIAL_KEY = "testNC";


    private final ConfidentialDocumentsSplitter underTest = new ConfidentialDocumentsSplitter();

    @Test
    void shouldNotAddEntryForNonConfidentialDocsWhenNoNonConfidentialDocsArePresent() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        List<Element<SupportingEvidenceBundle>> bundles = wrapElements(CONFIDENTIAL_BUNDLE);

        underTest.updateConfidentialDocsInCaseDetails(caseDetails, bundles, KEY);

        assertThat(caseDetails.getData()).isEmpty();
    }

    @Test
    void shouldRemoveEntryForNonConfidentialDocsWhenNoNonConfidentialDocsAreNoLongerPresent() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(NON_CONFIDENTIAL_KEY, wrapElements(NON_CONFIDENTIAL_BUNDLE));

        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        List<Element<SupportingEvidenceBundle>> bundles = wrapElements(CONFIDENTIAL_BUNDLE);

        underTest.updateConfidentialDocsInCaseDetails(caseDetails, bundles, KEY);

        assertThat(caseDetails.getData()).isEmpty();
    }

    @Test
    void shouldAddNonConfidentialDocsToOwnListWhenNonConfidentialDocsArePresent() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        List<Element<SupportingEvidenceBundle>> bundles = wrapElements(CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE);

        underTest.updateConfidentialDocsInCaseDetails(caseDetails, bundles, KEY);

        Map<String, Object> expectedSplit = Map.of(
            NON_CONFIDENTIAL_KEY, wrapElements(NON_CONFIDENTIAL_BUNDLE)
        );

        assertThat(caseDetails.getData()).isEqualTo(expectedSplit);
    }
}
