package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.UploadedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;

@Data
@Builder(toBuilder = true)
//1st page of create an order event: user selects type, document is hidden until check answers page
public class OrderTypeAndDocument {
    private final GeneratedOrderType type;
    private final GeneratedOrderSubtype subtype;
    private DocumentReference document;
    private final UploadedOrderType uploadedOrderType;
    private final String orderName;
    private final String orderDescription;

    @JsonIgnore
    public boolean isFinal() {
        return FINAL == subtype || (isUploaded() && uploadedOrderType.isFinal());
    }

    @JsonIgnore
    public boolean isInterim() {
        return INTERIM == subtype;
    }

    @JsonIgnore
    public boolean isClosable() {
        return isFinal() || EMERGENCY_PROTECTION_ORDER == type || DISCHARGE_OF_CARE_ORDER == type;
    }

    @JsonIgnore
    public boolean isUploaded() {
        return UPLOAD == type;
    }

    @JsonIgnore
    public String getTypeLabel() {
        if (!isUploaded()) {
            return type.getLabel();
        } else if (UploadedOrderType.OTHER == uploadedOrderType) {
            return orderName;
        }
        return uploadedOrderType.getFullLabel();
    }
}
