package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum PriorConsultationType {
    @CCD(
            label = "The other local authority is the authority providing the child[ren] with accommodation or on whose behalf the child[ren] [is] [are] being provided with accommodation."
    )
    PROVIDE_ACCOMMODATION("The other local authority is the authority providing the child[ren] with accommodation "
                          + "or on whose behalf the child[ren] [is] [are] being provided with accommodation."),
    @CCD(label = "The other local authority is the authority within whose area the child[ren] live[s], or will live.")
    WITHIN_THE_LIVING_AREA("The other local authority is the authority within whose area the child[ren] live[s], "
                           + "or will live.");

    private final String label;
}
