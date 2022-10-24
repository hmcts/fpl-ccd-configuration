package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SecureAccommodationOrderGround {
    ABSCOND_FROM_ACCOMMODATION("that the child[ren] [has] [have] a history of absconding and [is] [are] likely "
                               + "to abscond from any other accommodation and if the child[ren] abscond [he] [she] "
                               + "[they] [is] [are] likely to suffer significant harm.", 1),
    SELF_INJURY("that if the child[ren] [is] [are] kept in any other accommodation, [the child] [they] [is] "
                + "[are] likely to injure [himself] [herself] [themselves] or other people.", 2),
    APPROVAL_OF_SECRETARY_OF_STATE("(In the case of a child under the age of 13) The approval of the Secretary "
                                   + "of State to the placement of the child[ren] in secure accommodation has been "
                                   + "granted and is attached.", 3);

    private final String label;
    private final int displayOrder;
}