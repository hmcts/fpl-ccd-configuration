package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    C2_APPLICATION("C2"),
    C110A_APPLICATION("C110a");

    private final String type;
}
