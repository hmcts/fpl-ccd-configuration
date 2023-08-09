package uk.gov.hmcts.reform.fpl.service.noticeofproceedings;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;


@Component
class NoticeOfProceedingsLanguageFactoryTest {

    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;

    private static final LanguageTranslationRequirement ANOTHER_TRANSLATION_REQUIREMENTS =
        WELSH_TO_ENGLISH;

    private final NoticeOfProceedingsLanguageFactory underTest = new NoticeOfProceedingsLanguageFactory();

    @Test
    void testIfNoOrderIsPresent() {
        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
            .build());
        assertThat(actual).isEqualTo(NO);
    }

    @Test
    void testIfStandardOrderIsPresent() {
        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
                .standardDirectionOrder(StandardDirectionOrder.builder()
                    .translationRequirements(TRANSLATION_REQUIREMENTS)
                    .build())
            .build());

        assertThat(actual).isEqualTo(TRANSLATION_REQUIREMENTS);
    }

    @Test
    void testIfUrgentHearingPresent() {
        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
            .urgentHearingOrder(UrgentHearingOrder.builder()
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .build())
            .build());

        assertThat(actual).isEqualTo(TRANSLATION_REQUIREMENTS);
    }

    @Test
    void testIfUrgentHearingAndStoPresent() {
        LanguageTranslationRequirement actual = underTest.calculate(CaseData.builder()
            .urgentHearingOrder(UrgentHearingOrder.builder()
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .translationRequirements(ANOTHER_TRANSLATION_REQUIREMENTS)
                .build())
            .build());

        assertThat(actual).isEqualTo(ANOTHER_TRANSLATION_REQUIREMENTS);
    }
}

