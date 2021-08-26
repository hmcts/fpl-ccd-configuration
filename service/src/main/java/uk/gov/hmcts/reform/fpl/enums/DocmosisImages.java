package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum DocmosisImages {

    DRAFT_WATERMARK("[userImage:draft-watermark.png]", null),
    COURT_SEAL("[userImage:familycourtseal.png]", "[userImage:familycourtseal-bilingual.png]"),
    CREST("[userImage:crest.png]", null),
    HMCTS_LOGO_LARGE("[userImage:hmcts-logo-large.png]", null),
    HMCTS_LOGO_SMALL("[userImage:hmcts-logo-small.png]", null);

    private final String value;
    private final String valueWelsh;

    public Optional<String> getValueWelsh() {
        return Optional.ofNullable(valueWelsh);
    }

    public String getValue(Language language) {
        if (language == Language.WELSH) {
            return getValueWelsh().orElse(value);
        }
        return value;
    }
}
