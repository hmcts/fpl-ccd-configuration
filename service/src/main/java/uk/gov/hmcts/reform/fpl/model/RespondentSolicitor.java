package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
@Valid
public class RespondentSolicitor {
    @CCD(label = "Representative's first name")
    private String firstName;
    @CCD(label = "Representative's last name")
    private String lastName;
    @CCD(label = "Telephone number")
    private Telephone telephoneNumber;
    @CCD(label = "Email address", typeOverride = FieldType.Email)
    private String email;
    @CCD(label = "Organisation")
    private Organisation organisation;
    @CCD(label = "Managing office", showCondition = "organisation.OrganisationName!=\"\"")
    private Address regionalOfficeAddress;
    @CCD(label = "Organisation (unregistered)", showCondition = "unregisteredOrganisation.name!=\"\"")
    private UnregisteredOrganisation unregisteredOrganisation;
    @CCD(label = "Colleagues to be notified", showCondition = "unregisteredOrganisation = \"DO_NOT_SHOW\"")
    private List<Element<Colleague>> colleaguesToBeNotified;

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName)).trim();
    }

    @JsonIgnore
    public boolean hasFullName() {
        return isNotEmpty(firstName) && isNotEmpty(lastName);
    }

    @JsonIgnore
    public boolean hasOrganisationDetails() {
        return null != unregisteredOrganisation && isNotEmpty(unregisteredOrganisation.getName())
               || null != organisation && isNotEmpty(organisation.getOrganisationID());
    }
}
