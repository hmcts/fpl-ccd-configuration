package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum HearingNeedsBooked {
    @CCD(label = "Interpreter")
    INTERPRETER("Interpreter"),
    @CCD(label = "Spoken or written Welsh")
    SPOKEN_OR_WRITTEN_WELSH("Spoken or written Welsh"),
    @CCD(label = "Intermediary")
    INTERMEDIARY("Intermediary"),
    @CCD(label = "Facilities or assistance for a disability")
    FACILITIES_OR_ASSISTANCE("Facilities or assistance"),
    @CCD(label = "Separate waiting room or other security measures")
    SEPARATE_WAITING_OR_SECURITY_MEASURES("Separate waiting room or other security measures"),
    @CCD(label = "Something else")
    SOMETHING_ELSE("Something else"),
    @CCD(label = "None")
    NONE("None)");

    final String label;
}
