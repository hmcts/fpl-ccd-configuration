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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
//1st page of create an order event: user selects type, document is hidden until check answers page
public class OrderTypeAndDocument {
    @CCD(label = " ")
    private final GeneratedOrderType type;
    @CCD(label = " ", showCondition = "type=\"CARE_ORDER\" OR type=\"SUPERVISION_ORDER\"")
    private final GeneratedOrderSubtype subtype;
    @CCD(label = "Order document", typeOverride = FieldType.Document)
    private DocumentReference document;
    @CCD(
            label = "Select the type of order you're uploading",
            showCondition = "type=\"UPLOAD\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "UploadedOrderType"
    )
    private final UploadedOrderType uploadedOrderType;
    @CCD(label = "Order name", showCondition = "uploadedOrderType=\"OTHER\"")
    private final String orderName;
    @CCD(label = "Order description", showCondition = "uploadedOrderType=\"OTHER\"", typeOverride = FieldType.TextArea)
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
