package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Data
@AllArgsConstructor
public class Party {
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

    public String buildFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName));
    }
}
