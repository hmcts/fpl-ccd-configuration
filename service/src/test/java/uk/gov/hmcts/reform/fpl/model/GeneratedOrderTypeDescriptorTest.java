package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.UPLOAD;
import static uk.gov.hmcts.reform.fpl.model.GeneratedOrderTypeDescriptor.builder;

class GeneratedOrderTypeDescriptorTest {

    @ParameterizedTest
    @MethodSource("typeToEnumSource")
    void testConvertTypeStringToOrder(String type, GeneratedOrderTypeDescriptor orderTypeDescriptor) {
        assertThat(GeneratedOrderTypeDescriptor.fromType(type)).isEqualTo(orderTypeDescriptor);
    }

    @ParameterizedTest
    @MethodSource("typeToEnumSource")
    void testOrderTypesThatCanBeRemoved(
        String type,
        GeneratedOrderTypeDescriptor orderTypeDescriptor,
        boolean removable
    ) {
        assertThat(GeneratedOrderTypeDescriptor.fromType(type).isRemovable()).isEqualTo(removable);
    }

    private static Stream<Arguments> typeToEnumSource() {
        return Stream.of(
            Arguments.of("Blank order (C21)", builder().type(BLANK_ORDER).build(), true),
            Arguments.of("Interim care order", builder().type(CARE_ORDER).subtype(INTERIM).build(), true),
            Arguments.of("Final care order", builder().type(CARE_ORDER).subtype(FINAL).build(), false),
            Arguments.of("Interim supervision order",
                builder().type(SUPERVISION_ORDER).subtype(INTERIM).build(), true),
            Arguments.of("Final supervision order", builder().type(SUPERVISION_ORDER).subtype(FINAL).build(), false),
            Arguments.of("Emergency protection order", builder().type(EMERGENCY_PROTECTION_ORDER).build(), false),
            Arguments.of("Discharge of care order", builder().type(DISCHARGE_OF_CARE_ORDER).build(),false),
            Arguments.of("Upload", builder().type(UPLOAD).build(),false),
            Arguments.of("Upload something blah", builder().type(UPLOAD).build(),false)
        );
    }
}
