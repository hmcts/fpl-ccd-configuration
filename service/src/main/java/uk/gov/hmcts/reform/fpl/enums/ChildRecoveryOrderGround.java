package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildRecoveryOrderGroundList", generate = true)
@Getter
@RequiredArgsConstructor
public enum ChildRecoveryOrderGround {
    @CCD(
            label = "[has] [have] been unlawfully taken away or [is] [are]being unlawfully kept away from the responsible person"
    )
    UNLAWFULLY_TAKEN_AWAY("[has] [have] been unlawfully taken away or [is] [are] being unlawfully kept away from "
                          + "the responsible person", 1),
    @CCD(label = "[has] [have] run away or [is] [are] staying away from the responsible person")
    RUN_AWAY_FROM_RESPONSIBLE_PERSON("[has] [have] run away or [is] [are] staying away from the responsible person", 2),
    @CCD(label = "[is] [are] missing.")
    IS_MISSING("[is] [are] missing.", 3);

    private final String label;
    private final int displayOrder;
}
