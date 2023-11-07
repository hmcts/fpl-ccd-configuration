package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum C2ApplicationType {
    WITH_NOTICE("With notice"),
    WITHOUT_NOTICE("By consent");

    private final String label;
}
