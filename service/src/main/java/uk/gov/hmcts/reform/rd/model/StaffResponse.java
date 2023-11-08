package uk.gov.hmcts.reform.rd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffResponse {

    @JsonProperty("ccd_service_name")
    private String ccdServiceName;

    @JsonProperty(value = "staff_profile")
    private StaffProfile staffProfile;

}
