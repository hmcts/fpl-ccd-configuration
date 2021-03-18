package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class AdditionalOrgDetails {
    private String newOrganisationRequest;
    private String newOrganisationName;
    private Address newOrgAddress;
    private String additionalOffice;
    private Address regionalOfficeAddress;
}
