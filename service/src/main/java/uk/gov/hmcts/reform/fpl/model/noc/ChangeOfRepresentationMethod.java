package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChangeOfRepresentationMethod {
    NOC("Notice of change"),
    RESPONDENTS_EVENT("FPL");

    private final String label;
}
