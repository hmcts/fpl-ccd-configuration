package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PBAPayment {
    @CCD(label = "Do you want to enter PBA details?", typeOverride = FieldType.YesOrNo)
    private String usePbaPayment;
    @CCD(label = "Payment by account (PBA) number", hint = "For example, PBA1234567")
    private String pbaNumber;
    @CCD(
            label = "Payment by account (PBA) number",
            hint = "For example, PBA1234567",
            typeOverride = FieldType.DynamicList
    )
    @Temp
    private DynamicList pbaNumberDynamicList;
    @CCD(label = "Client code")
    private String clientCode;
    @CCD(label = "Customer reference", hint = "Add a meaningful reference to help with your reconciliation.")
    private String fileReference;
}
