package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.MaskHelper.maskEmail;

class MaskHelperTest {

    @Test
    void shouldReturnMaskedEmail() {
        assertThat(MaskHelper.maskEmail(null)).isEmpty();
        assertThat(MaskHelper.maskEmail("")).isEmpty();
        assertThat(MaskHelper.maskEmail("abc")).isEqualTo("***");
        assertThat(MaskHelper.maskEmail("abc@t")).isEqualTo("***@*");
    }

    @Test
    void shouldMaskEmailInText() {
        String email = "test@test.com";
        String text = "The email is test@test.com";

        assertThat(maskEmail(text, email)).isEqualTo("The email is ****@********");
    }

    @Test
    void shouldMaskNotMaskEmailIfNotPresentInText() {
        String email = "test@test.com";
        String text = "Text without email";

        assertThat(maskEmail(text, email)).isEqualTo("Text without email");
    }

    @Test
    void shouldMaskEmailMultipleTimes() {
        String email = "test@test.com";
        String text = "Email sent to test@test.com. Reply to test@test.com";

        assertThat(maskEmail(text, email)).isEqualTo("Email sent to ****@********. Reply to ****@********");
    }

    @Test
    void shouldNotMaskEmailInEmptyString() {
        assertThat(maskEmail(null, "test@test.com")).isNull();
        assertThat(maskEmail("", "test@test.com")).isEmpty();
    }

}
