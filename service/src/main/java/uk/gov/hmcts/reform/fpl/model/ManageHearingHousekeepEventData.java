package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.HearingHousekeepReason;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;

@Data
@Builder
public class ManageHearingHousekeepEventData {
    @CCD(
            label = "Is this for Housekeeping?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private YesNo hearingHousekeepOption;
    @CCD(
            label = "Housekeeping reason",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class}
    )
    private HearingHousekeepReason hearingHousekeepReason;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class})
    private String hearingHousekeepReasonOther;
}
