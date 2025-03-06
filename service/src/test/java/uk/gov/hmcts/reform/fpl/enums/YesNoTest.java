package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

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
        assertThat(YesNo.isYesOrNo("Yes")).isEqualTo(true);
        assertThat(YesNo.isYesOrNo("Nop")).isEqualTo(false);
    }

    @Test
    void shouldReturnLanguageValueIfValid() {
        assertThat(NO.getValue(Language.ENGLISH)).isEqualTo("No");
        assertThat(NO.getValue(Language.WELSH)).isEqualTo("Na");
    }
}
