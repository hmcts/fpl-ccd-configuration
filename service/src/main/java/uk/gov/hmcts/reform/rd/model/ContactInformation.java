package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.Address;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformation {
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String country;
    private String county;
    private ArrayList<DxAddress> dxAddress;
    private String postCode;
    private String townCity;

    @JsonIgnore
    public Address getContactInformationAsAddress() {
        return Address.builder()
            .addressLine1(getAddressLine1())
            .addressLine2(getAddressLine2())
            .addressLine3(getAddressLine3())
            .county(getCounty())
            .country(getCountry())
            .postcode(getPostCode())
            .postTown(getTownCity())
            .build();
    }
}
