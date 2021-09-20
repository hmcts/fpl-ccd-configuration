package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import static org.assertj.core.api.Assertions.assertThat;

class SealedOrderLanguageRequirementGeneratorTest {

    private final SealedOrderLanguageRequirementGenerator underTest = new SealedOrderLanguageRequirementGenerator();


    @Test
    void testWhenSpecifiedByUserNO() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersTranslationNeeded(LanguageTranslationRequirement.NO)
                    .build())
            .build());

        assertThat(actual).isEqualTo(LanguageTranslationRequirement.NO);
    }

    @Test
    void testWhenSpecifiedByUserWelshToEnglish() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersTranslationNeeded(LanguageTranslationRequirement.WELSH_TO_ENGLISH)
                .build())
            .build());

        assertThat(actual).isEqualTo(LanguageTranslationRequirement.WELSH_TO_ENGLISH);
    }

    @Test
    void testWhenSpecifiedByUserEnglishToWelsh() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersTranslationNeeded(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                .build())
            .build());

        assertThat(actual).isEqualTo(LanguageTranslationRequirement.ENGLISH_TO_WELSH);
    }

    @Test
    void testWhenLanguageRequirementDecisionEmpty() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
            .build());
        assertThat(actual).isEqualTo(LanguageTranslationRequirement.NO);
    }

    @Test
    void testWhenLanguageRequirementDecisionNo() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
                .languageRequirement(YesNo.NO.getValue())
            .build());
        assertThat(actual).isEqualTo(LanguageTranslationRequirement.NO);

    }

    @Test
    void testWhenLanguageRequirementDecisionYes() {
        LanguageTranslationRequirement actual = underTest.translationRequirements(CaseData.builder()
            .languageRequirement(YesNo.YES.getValue())
            .build());
        assertThat(actual).isEqualTo(LanguageTranslationRequirement.ENGLISH_TO_WELSH);
    }

}
