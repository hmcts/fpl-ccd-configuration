package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.time.LocalDate;

import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Party {
    // REFACTOR: 03/12/2019 This needs to be private, effects tests as well
    public final String partyId;
    public final PartyType partyType;
    public final Address address;

}
