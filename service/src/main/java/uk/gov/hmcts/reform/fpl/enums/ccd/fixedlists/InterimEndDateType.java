package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterimEndDateType {
    END_OF_PROCEEDINGS("End of the proceedings"),
    NAMED_DATE("A named date");

    private final String label;
}
