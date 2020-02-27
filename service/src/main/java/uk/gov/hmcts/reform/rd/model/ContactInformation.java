package uk.gov.hmcts.reform.rd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

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
}
