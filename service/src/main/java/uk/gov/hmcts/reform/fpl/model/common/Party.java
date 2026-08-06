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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Party implements Recipient {
    @CCD(label = "Party ID", showCondition = "partyType=\"DO_NOT_SHOW\"")
    protected final String partyId;
    @CCD(label = " ", showCondition = "partyType=\"DO_NOT_SHOW\"")
    protected final PartyType partyType;
    @CCD(label = "First name")
    protected final String firstName;
    @CCD(label = "Last name")
    protected final String lastName;
    @CCD(label = "Name of applicant", hint = "Local authority or authorised person")
    protected final String organisationName;
    @CCD(label = "Date of birth", hint = "For example, 31 3 1980")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected final LocalDate dateOfBirth;
    @CCD(
            label = "Current address",
            showCondition = "addressKnow=\"Yes\" OR addressKnow=\"LIVE_IN_REFUGE\"",
            typeOverride = FieldType.AddressUK
    )
    protected final Address address;


    @CCD(label = "Email")
    @Valid
    protected final EmailAddress email;
    @CCD(label = " ")
    protected final Telephone telephoneNumber;

    @JsonIgnore
    public String getFullName() {
        return String.format("%s %s", defaultString(firstName), defaultString(lastName)).trim();
    }
}
