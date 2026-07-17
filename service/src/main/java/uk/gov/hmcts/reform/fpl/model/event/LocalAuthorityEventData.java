package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORASOLICITORACrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus3RolesApwidhAccess;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class LocalAuthorityEventData {

    @CCD(
            label = "Applicant details",
            searchable = false,
            access = {CHILDSOLICITORASOLICITORACrudAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    private LocalAuthority localAuthority;
    @CCD(
            label = "Main contact",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CHILDSOLICITORACrudPlus3RolesApwidhAccess.class}
    )
    @Temp
    private Colleague applicantContact;
    @CCD(
            label = "Person",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantContactOther",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CHILDSOLICITORACrudPlus3RolesApwidhAccess.class}
    )
    @Temp
    private List<Element<Colleague>> applicantContactOthers;
}
