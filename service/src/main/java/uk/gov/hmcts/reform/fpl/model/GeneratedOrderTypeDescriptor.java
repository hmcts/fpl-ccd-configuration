package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

@Data
@Builder
public class GeneratedOrderTypeDescriptor {

    private final GeneratedOrderType type;
    private final GeneratedOrderSubtype subtype;

    public static GeneratedOrderTypeDescriptor fromType(String type) {
        return GeneratedOrderTypeDescriptor.builder()
            .type(GeneratedOrderType.fromType(type))
            .subtype(GeneratedOrderSubtype.fromType(type).orElse(null))
            .build();
    }

}
