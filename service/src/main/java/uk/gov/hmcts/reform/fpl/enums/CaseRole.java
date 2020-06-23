package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.ccd.sdk.types.ComplexType;
import uk.gov.hmcts.ccd.sdk.types.FixedList;

@FixedList
public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    SOLICITOR,
    LABARRISTER,
    BARRISTER,
    CAFCASSSOLICITOR;

    private String formattedName;

    CaseRole() {
        this.formattedName = String.format("[%s]", name());
    }

    public String formattedName() {
        return formattedName;
    }
}
