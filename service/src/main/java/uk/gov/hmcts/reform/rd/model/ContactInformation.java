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
    private String townCity;
    private String county;
    private String postcode;
}
