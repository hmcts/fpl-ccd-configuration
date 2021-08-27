package uk.gov.hmcts.reform.fpl.service.translation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.group.C110A;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;

class C110ATranslationRequirementCalculatorTest {

    private final C110ATranslationRequirementCalculator underTest = new C110ATranslationRequirementCalculator();

    @Test
    void calculateIfEmpty() {
        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
            .c110A(C110A.builder().build())
            .build());

        assertThat(actual).isEqualTo(NO);
    }

    @ParameterizedTest
    @MethodSource("permutations")
    void calculate(String languageRequirement, Language languageRequirementApplication, String needEnglishTranslation,
                   String needWelshTranslation, LanguageTranslationRequirement expected) {

        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
            .languageRequirement(languageRequirement)
            .c110A(C110A.builder()
                .languageRequirementApplication(languageRequirementApplication)
                .languageRequirementApplicationNeedEnglish(needEnglishTranslation)
                .languageRequirementApplicationNeedWelsh(needWelshTranslation)
                .build())
            .build());

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> permutations() {
        return Stream.of(
            Arguments.of("No", null, null, null, NO),
            Arguments.of("Yes", Language.ENGLISH, null, "Yes", ENGLISH_TO_WELSH),
            Arguments.of("Yes", Language.ENGLISH, null, "No", NO),
            Arguments.of("Yes", Language.WELSH, "Yes", null, WELSH_TO_ENGLISH),
            Arguments.of("Yes", Language.WELSH, "No", null, NO)
        );
    }
}
