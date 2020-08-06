package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingOptionsPOCType {
    NEW_HEARING("Create a new hearing"),
    EDIT_DRAFT("Edit a draft hearing"),
    EDIT_ADJOURNED("Edit an adjourned hearing");

    private final String label;
}
