package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus25RolesDalfnpAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrdAccess;

@Builder(toBuilder = true)
@Value
@Jacksonized
public class ManageLegalCounselEventData {

    @CCD(
            label = "Counsel",
            searchable = false,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, BARRISTERCrdAccess.class}
    )
    List<Element<LegalCounsellor>> legalCounsellors;

}
