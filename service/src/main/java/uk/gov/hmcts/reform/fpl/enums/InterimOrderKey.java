package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterimOrderKey {
    INTERIM_END_DATE("interimEndDate");

    private final String key;
}
