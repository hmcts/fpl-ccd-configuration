package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatekeepingOrderSealDecision {
    @CCD(label = " ", typeOverride = FieldType.Document)
    private final DocumentReference draftDocument;
    @CCD(label = "Order issue date", showCondition = "orderStatus=\"SEALED\"")
    private final LocalDate dateOfIssue;
    @CCD(label = "Do you want to send this order now?")
    private final OrderStatus orderStatus;
    @CCD(label = "Next steps", showCondition = "orderStatus=\"DO_NOT_SHOW\"")
    private final String nextSteps;

    @JsonIgnore
    public boolean isSealed() {
        return orderStatus == SEALED;
    }
}
