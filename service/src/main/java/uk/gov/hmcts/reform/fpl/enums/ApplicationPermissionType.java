package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationPermissionType {
    YES("Yes"),
    NOT_REQUIRED("Permission not required"),
    ALREADY_GRANTED("Permission already granted");

    private final String label;
}
