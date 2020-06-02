package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AllocatedJudgeNotificationType {
    CMO("cmo"),
    SDO("sdo"),
    GENERATED_ORDER("generated-order"),
    NOTICE_OF_PROCEEDINGS("notice-of-proceedings"),
    C2_APPLICATION("c2");

    private final String value;
}
