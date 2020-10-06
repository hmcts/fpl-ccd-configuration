package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingOptions {
    NEW_HEARING("Add a new hearing"),
    EDIT_DRAFT("Edit or issue a draft hearing");

    private final String label;
}
