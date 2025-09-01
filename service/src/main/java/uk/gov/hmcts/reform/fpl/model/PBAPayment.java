package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PBAPayment {
    private String usePbaPayment;
    private String pbaNumber;
    private String clientCode;
    private String fileReference;
}
