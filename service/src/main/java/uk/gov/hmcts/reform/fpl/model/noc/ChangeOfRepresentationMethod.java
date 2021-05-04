package uk.gov.hmcts.reform.fpl.model.noc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChangeOfRepresentationMethod {
    NOC("Noc"),
    RESPONDENTS_EVENT("FPLA");

    private final String label;
}
