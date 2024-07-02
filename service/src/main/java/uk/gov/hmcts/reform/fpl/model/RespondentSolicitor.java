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

@Data
@Builder(toBuilder = true)
@Jacksonized
@Valid
public class RespondentSolicitor {
    private String firstName;
    private String lastName;
    private Telephone telephoneNumber;
    private String email;
    private Organisation organisation;
    private Address regionalOfficeAddress;
    private UnregisteredOrganisation unregisteredOrganisation;
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
