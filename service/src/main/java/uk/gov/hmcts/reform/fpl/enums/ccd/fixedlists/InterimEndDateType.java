package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum InterimEndDateType {
    @CCD(label = "At the end of the proceedings, or until a further order is made")
    END_OF_PROCEEDINGS("At the end of the proceedings, or until a further order is made"),
    @CCD(label = "At the end of a named date")
    NAMED_DATE("A named date"),
    @CCD(label = "At a specific time on a named date")
    SPECIFIC_TIME_NAMED_DATE("A specific time on a named date");

    private final String label;
}
