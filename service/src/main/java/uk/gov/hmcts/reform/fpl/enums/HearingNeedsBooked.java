package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingNeedsBooked {
    INTERPRETER("Interpreter"),
    SPOKEN_OR_WRITTEN_WELSH("Spoken or written Welsh"),
    INTERMEDIARY("Intermediary"),
    FACILITIES_OR_ASSISTANCE("Facilities or assistance"),
    SEPARATE_WAITING_OR_SECURITY_MEASURES("Separate waiting room or other security measures"),
    SOMETHING_ELSE("Something else"),
    NONE("None)");

    final String label;
}
