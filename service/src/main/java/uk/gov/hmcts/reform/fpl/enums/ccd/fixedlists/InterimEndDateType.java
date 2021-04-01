package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterimEndDateType {
    END_OF_PROCEEDINGS("At the end of the proceedings, or until a further order is made"),
    NAMED_DATE("A named date"),
    SPECIFIC_TIME_NAMED_DATE("A specific time on a named date");

    private final String label;
}
