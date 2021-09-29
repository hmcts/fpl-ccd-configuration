package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;

class DocmosisImagesTest {

    @Test
    void testSpecifyLanguageEnglish() {
        assertThat(COURT_SEAL.getValue(Language.ENGLISH)).isEqualTo(COURT_SEAL.getValue());
    }

    @Test
    void testSpecifyLanguageWelsh() {
        assertThat(COURT_SEAL.getValue(Language.WELSH)).isEqualTo(COURT_SEAL.getValueWelsh().get());
    }

    @Test
    void testSpecifyLanguageWelshIfNoWelshImage() {
        assertThat(CREST.getValue(Language.WELSH)).isEqualTo(CREST.getValue());
    }

}
