package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Recipient;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Party implements Recipient {
    protected final String partyId;
    protected final PartyType partyType;
    protected final String firstName;
    protected final String lastName;
    protected final String organisationName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected final LocalDate dateOfBirth;
    protected final Address address;


    @Valid
    protected final EmailAddress email;
    protected final Telephone telephoneNumber;

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName)).trim();
    }
}
