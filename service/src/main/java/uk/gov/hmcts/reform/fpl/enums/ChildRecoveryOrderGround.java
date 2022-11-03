package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChildRecoveryOrderGround {
    UNLAWFULLY_TAKEN_AWAY("[has] [have] been unlawfully taken away or [is] [are] being unlawfully kept away from "
                          + "the responsible person", 1),
    RUN_AWAY_FROM_RESPONSIBLE_PERSON("[has] [have] run away or [is] [are] staying away from the responsible person", 2),
    IS_MISSING("[is] [are] missing.", 3);

    private final String label;
    private final int displayOrder;
}
