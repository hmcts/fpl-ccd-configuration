package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;

@Data
@Builder(toBuilder = true)
//1st page of create an order event: user selects type, document is hidden until check answers page
public class OrderTypeAndDocument {
    private final GeneratedOrderType type;
    private final GeneratedOrderSubtype subtype;
    private final DocumentReference document;

    @JsonIgnore
    public String getFullType() {
        return getFullType(null);
    }

    @JsonIgnore
    public String getFullType(GeneratedOrderSubtype subtype) {
        return (subtype != null) ? subtype.getLabel() + " " + this.type.getLabel().toLowerCase() : this.type.getLabel();
    }

    public boolean hasInterimSubtype() {
        return subtype == INTERIM;
    }
}
