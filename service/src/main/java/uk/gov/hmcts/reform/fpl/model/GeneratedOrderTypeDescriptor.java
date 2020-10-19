package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class GeneratedOrderTypeDescriptor {

    private final GeneratedOrderType type;
    private final GeneratedOrderSubtype subtype;

    public Optional<GeneratedOrderSubtype> getSubtype() {
        return Optional.ofNullable(subtype);
    }

    public static GeneratedOrderTypeDescriptor fromType(String type) {
        return GeneratedOrderTypeDescriptor.builder()
            .type(GeneratedOrderType.fromType(type))
            .subtype(GeneratedOrderSubtype.fromType(type).orElse(null))
            .build();
    }

    public boolean isRemovable() {
        return (type == GeneratedOrderType.BLANK_ORDER)
            || (type == GeneratedOrderType.CARE_ORDER && subtype == GeneratedOrderSubtype.INTERIM)
            || (type == GeneratedOrderType.SUPERVISION_ORDER && subtype == GeneratedOrderSubtype.INTERIM);
    }

}
