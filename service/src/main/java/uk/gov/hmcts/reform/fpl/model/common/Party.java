package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Party {
    // REFACTOR: 03/12/2019 This needs to be private, effects tests as well
    public final String partyId;
    public final PartyType partyType;
    public final String firstName;
    public final String lastName;
    public final String organisationName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public final LocalDate dateOfBirth;
    public final Address address;
    public final EmailAddress email;
    public final Telephone telephoneNumber;

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName)).trim();
    }
}
