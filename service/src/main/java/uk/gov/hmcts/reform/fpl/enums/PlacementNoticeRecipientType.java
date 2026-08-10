package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum PlacementNoticeRecipientType {

    @CCD(label = "Local authority")
    LOCAL_AUTHORITY("Local authority"),
    @CCD(label = "Respondent")
    RESPONDENT("Respondents (Parents)"),
    @CCD(label = "Cafcass")
    CAFCASS("Cafcass");

    private final String name;
}
