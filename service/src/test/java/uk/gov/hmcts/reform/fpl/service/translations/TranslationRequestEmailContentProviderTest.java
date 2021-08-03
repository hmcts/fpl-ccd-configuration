package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TranslationRequestEmailContentProviderTest {

    private final TranslationRequestEmailContentProvider underTest = new TranslationRequestEmailContentProvider();

    @Test
    void testEnglishToWelsh() {
        String actual = underTest.generate(LanguageTranslationRequirement.ENGLISH_TO_WELSH);

        assertThat(actual).isEqualTo(
            "Please find attached the following documents for translation to "
                + "Welsh\n"
                + " Family Public Law digital service\n"
                + "\n"
                + " HM Courts & Tribunals Service\n"
                + "\n"
                + "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                + "contactfpl@justice"
                + ".gov.uk");
    }

    @Test
    void testWelshToEnglish() {
        String actual = underTest.generate(LanguageTranslationRequirement.WELSH_TO_ENGLISH);

        assertThat(actual).isEqualTo(
            "Please find attached the following documents for translation to "
                + "English\n"
                + " Family Public Law digital service\n"
                + "\n"
                + " HM Courts & Tribunals Service\n"
                + "\n"
                + "Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                + "contactfpl@justice"
                + ".gov.uk");
    }

    @Test
    void tesNoTranslation() {
        assertThrows(IllegalArgumentException.class, () -> underTest.generate(LanguageTranslationRequirement.NO));
    }


}
