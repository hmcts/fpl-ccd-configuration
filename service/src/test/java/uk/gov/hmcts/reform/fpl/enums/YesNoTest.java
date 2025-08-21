package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
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

    @Test
    void shouldCheckIfValueIsYesNo() {
        assertThat(YesNo.isYesOrNo("Yes")).isTrue();
        assertThat(YesNo.isYesOrNo("Nop")).isFalse();
    }

    @Test
    void shouldReturnLanguageValueIfValid() {
        assertThat(NO.getValue(Language.ENGLISH)).isEqualTo("No");
        assertThat(NO.getValue(Language.WELSH)).isEqualTo("Na");
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
