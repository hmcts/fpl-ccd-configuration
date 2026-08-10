package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SecureAccommodationOrderGroundList", generate = true)
@Getter
@RequiredArgsConstructor
public enum SecureAccommodationOrderGround {
    @CCD(
            label = "that the child[ren] [has] [have] a history of absconding and [is] [are] likely to abscond from any other accommodation and if the child[ren] abscond [he] [she] [they] [is] [are] likely to suffer significant harm."
    )
    ABSCOND_FROM_ACCOMMODATION("that the child[ren] [has] [have] a history of absconding and [is] [are] likely "
                               + "to abscond from any other accommodation and if the child[ren] abscond [he] [she] "
                               + "[they] [is] [are] likely to suffer significant harm.", 1),
    @CCD(
            label = "that if the child[ren] [is] [are] kept in any other accommodation, [the child] [they] [is] [are] likely to injure [himself] [herself] [themselves] or other people."
    )
    SELF_INJURY("that if the child[ren] [is] [are] kept in any other accommodation, [the child] [they] [is] "
                + "[are] likely to injure [himself] [herself] [themselves] or other people.", 2),
    @CCD(
            label = "(In the case of a child under the age of 13) The approval of the Secretary of State to the placement of the child[ren] in secure accommodation has been granted and is attached."
    )
    APPROVAL_OF_SECRETARY_OF_STATE("(In the case of a child under the age of 13) The approval of the Secretary "
                                   + "of State to the placement of the child[ren] in secure accommodation has been "
                                   + "granted and is attached.", 3);

    private final String label;
    private final int displayOrder;
}