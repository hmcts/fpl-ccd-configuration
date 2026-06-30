package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationPermissionType {
    YES("Yes"),
    ALREADY_GRANTED("Permission already granted"),
    NOT_REQUIRED("Permission not required");

    private final String label;
}
