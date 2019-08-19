package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Party {
    public final String partyId;
    public final PartyType partyType;
    public final String firstName;
    public final String lastName;
    public final String organisationName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public final Date dateOfBirth;
    @Valid
    @NotNull(message = "Enter a valid address for the contact")
    public final Address address;
    @NotNull(message = "Enter an email address for the contact")
    @Valid
    public final EmailAddress email;
    public final Telephone telephoneNumber;
}
