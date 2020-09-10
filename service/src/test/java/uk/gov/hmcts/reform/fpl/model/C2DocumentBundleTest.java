package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

import static org.assertj.core.api.Assertions.assertThat;

class C2DocumentBundleTest {
    @Test
    void shouldFormatC2DocumentBundleToLabel() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .uploadedDateTime("1st June 2019")
            .build();

        String label = c2DocumentBundle.toLabel(1);

        assertThat(label).isEqualTo("Application 1: 1st June 2019");
    }
}
