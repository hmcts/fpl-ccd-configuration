package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationProposalMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private AllocationProposalMappingFilter filter = new AllocationProposalMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "LAY_JUSTICES, 'Lay justices'",
        "DISTRICT_JUDGE, 'District judge'",
        "CIRCUIT_JUDGE, 'Circuit judge'",
        "SECTION_9_CIRCUIT_JUDGE, 'Section 9 circuit judge'",
        "HIGH_COURT_JUDGE, 'High court judge'",
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.AllocationProposalListType."
                + incorrectEnum);
    }
}
