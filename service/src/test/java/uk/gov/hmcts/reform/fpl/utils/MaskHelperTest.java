package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.MaskHelper.maskEmail;

class MaskHelperTest {

    private static Stream<Arguments> createMaskEmailTestData() {
        return Stream.of(
            Arguments.of("The email is test@test.com", "The email is ****@********"),
            Arguments.of("Text without email", "Text without email"),
            Arguments.of(
                "Email sent to test@test.com. Reply to test@test.com",
                "Email sent to ****@********. Reply to ****@********"
            )
        );
    }

    @Test
    void shouldReturnMaskedEmail() {
        assertThat(MaskHelper.maskEmail(null)).isEmpty();
        assertThat(MaskHelper.maskEmail("")).isEmpty();
        assertThat(MaskHelper.maskEmail("abc")).isEqualTo("***");
        assertThat(MaskHelper.maskEmail("abc@t")).isEqualTo("***@*");
    }

    @ParameterizedTest
    @MethodSource("createMaskEmailTestData")
    void shouldMaskEmailInText(String text, String expected) {
        String email = "test@test.com";

        assertThat(maskEmail(text, email)).isEqualTo(expected);
    }

    @Test
    void shouldNotMaskEmailInEmptyString() {
        assertThat(maskEmail(null, "test@test.com")).isNull();
        assertThat(maskEmail("", "test@test.com")).isEmpty();
    }

}
