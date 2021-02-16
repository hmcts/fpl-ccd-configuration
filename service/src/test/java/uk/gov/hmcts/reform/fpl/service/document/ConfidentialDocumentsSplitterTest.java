package uk.gov.hmcts.reform.fpl.service.document;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
        .name("Confidential doc")
        .confidential(null)
        .build();

    private static final String KEY = "test";
    private static final String NON_CONFIDENTIAL_KEY = "testNC";


    private final ConfidentialDocumentsSplitter underTest = new ConfidentialDocumentsSplitter();

    @Test
    void shouldReturnEmptyListNonConfidentialDocsWhenNoNonConfidentialDocsArePresent() {
        List<Element<SupportingEvidenceBundle>> bundles = wrapElements(CONFIDENTIAL_BUNDLE);

        Map<String, Object> splitBundles = underTest.splitIntoAllAndNonConfidential(bundles, KEY);

        Map<String, Object> expectedSplit = Map.of(
            KEY, bundles,
            NON_CONFIDENTIAL_KEY, List.of()
        );

        assertThat(splitBundles).isEqualTo(expectedSplit);
    }

    @Test
    void shouldAddNonConfidentialDocsToOwnListWhenNonConfidentialDocsArePresent() {
        List<Element<SupportingEvidenceBundle>> bundles = wrapElements(CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE);

        Map<String, Object> splitBundles = underTest.splitIntoAllAndNonConfidential(bundles, KEY);

        Map<String, Object> expectedSplit = Map.of(
            KEY, bundles,
            NON_CONFIDENTIAL_KEY, wrapElements(NON_CONFIDENTIAL_BUNDLE)
        );

        assertThat(splitBundles).isEqualTo(expectedSplit);
    }
}
