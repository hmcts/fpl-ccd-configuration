package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SupplementaryDataUpdate {

    @JsonProperty("$set")
    private HmctsServiceID hmctsServiceId;
}
