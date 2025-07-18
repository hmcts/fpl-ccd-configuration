package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PBAPayment {
    private String usePbaPayment;
    private String pbaNumber;
    @Temp
    private DynamicList pbaNumberDynamicList;
    private String clientCode;
    private String fileReference;
}
