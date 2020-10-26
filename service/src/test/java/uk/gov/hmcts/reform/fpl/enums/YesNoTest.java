package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

class YesNoTest {

    @Test
    void shouldConvertBooleanToEnum() {
        assertThat(YesNo.from(true)).isEqualTo(YES);
        assertThat(YesNo.from(false)).isEqualTo(NO);
    }
}
