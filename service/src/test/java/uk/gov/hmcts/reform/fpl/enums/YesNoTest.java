package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

class YesNoTest {

    @Test
    void shouldConvertBooleanToEnum() {
        assertThat(YesNo.from(true)).isEqualTo(YES);
        assertThat(YesNo.from(false)).isEqualTo(NO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Yes", "YES", "yes"})
    void shouldCompareYesString(String yesString) {
        assertThat(YES.equalsString(yesString)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"No", "NO", "no"})
    void shouldCompareNoString(String noString) {
        assertThat(NO.equalsString(noString)).isTrue();
    }

}
