package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CafcassApiAddress {
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postTown;
    private final String county;
    private final String postcode;
    private final String country;
}