package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.BooleanHelper.booleanToYesNo;

public class BooleanHelperTest {

    @Test
    void shouldReturnYesWhenTrue() {
        assertThat(booleanToYesNo(true)).isEqualTo("Yes");
    }

    @Test
    void shouldReturnNoWhenFalse() {
        assertThat(booleanToYesNo(false)).isEqualTo("No");
    }
}
