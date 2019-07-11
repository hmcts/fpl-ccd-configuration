package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelephoneNumber {
    private final String telephoneNumber;
    private final String telephoneUsageType;
    private final String contactDirection;

    //TODO: remove jsonCreator and property annotations

    @JsonCreator
    private TelephoneNumber(@JsonProperty("telephoneNumber") String telephoneNumber,
                            @JsonProperty("telephoneUsageType") String telephoneUsageType,
                            @JsonProperty("contactDirection") String contactDirection) {
        this.telephoneNumber = telephoneNumber;
        this.telephoneUsageType = telephoneUsageType;
        this.contactDirection = contactDirection;
    }
}
