package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PriorConsultationType {
    PROVIDE_ACCOMMODATION("The other local authority is the authority providing the child[ren] with accommodation "
                          + "or on whose behalf the child[ren] [is] [are] being provided with accommodation."),
    WITHIN_THE_LIVING_AREA("The other local authority is the authority within whose area the child[ren] live[s], "
                           + "or will live.");

    private final String label;
}
