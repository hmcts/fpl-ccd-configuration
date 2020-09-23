package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisRepresentedBy {
    @JsonProperty("representativeName")
    private final String name;
    @JsonProperty("representativeEmail")
    private final String email;
    @JsonProperty("representativePhoneNumber")
    private final String phoneNumber;
}
