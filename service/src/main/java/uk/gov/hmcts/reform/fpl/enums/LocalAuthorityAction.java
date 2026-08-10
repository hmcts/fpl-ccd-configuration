package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum LocalAuthorityAction {
    @CCD(label = "Give case access to another local authority")
    ADD,
    @CCD(label = "Remove case access from local authority")
    REMOVE,
    @CCD(label = "Transfer the case to another local authority")
    TRANSFER,
    @CCD(label = "Transfer to another Court")
    TRANSFER_COURT;
}
