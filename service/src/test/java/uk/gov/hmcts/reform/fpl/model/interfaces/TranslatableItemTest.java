package uk.gov.hmcts.reform.fpl.model.interfaces;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;

class TranslatableItemTest {

    @Test
    void needTranslationIfNull() {
        assertThat(new TestImplementation(null).getNeedTranslation()).isEqualTo(YesNo.NO);
    }

    @Test
    void needTranslationIfNO() {
        assertThat(new TestImplementation(NO).getNeedTranslation()).isEqualTo(YesNo.NO);
    }

    @Test
    void needTranslationIfEnglishToWelsh() {
        assertThat(new TestImplementation(ENGLISH_TO_WELSH).getNeedTranslation()).isEqualTo(YesNo.YES);
    }

    @Test
    void needTranslationIfWelshToEnglish() {
        assertThat(new TestImplementation(WELSH_TO_ENGLISH).getNeedTranslation()).isEqualTo(YesNo.YES);
    }

    private static class TestImplementation implements TranslatableItem {

        private final LanguageTranslationRequirement translationRequirements;

        public TestImplementation(LanguageTranslationRequirement translationRequirements) {
            this.translationRequirements = translationRequirements;
        }

        @Override
        public boolean hasBeenTranslated() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public LocalDateTime translationUploadDateTime() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public DocumentReference getTranslatedDocument() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public LanguageTranslationRequirement getTranslationRequirements() {
            return translationRequirements;
        }

        @Override
        public String asLabel() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public String getModifiedItemType() {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public List<Element<Other>> getSelectedOthers() {
            throw new RuntimeException("Not implemented");
        }
    }

}
