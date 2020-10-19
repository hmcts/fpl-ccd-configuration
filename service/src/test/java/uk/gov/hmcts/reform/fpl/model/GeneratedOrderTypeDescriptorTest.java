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

    private static Stream<Arguments> typeToEnumSource() {
        return Stream.of(
            Arguments.of("Blank order (C21)", builder().type(BLANK_ORDER).build()),
            Arguments.of("Interim care order", builder().type(CARE_ORDER).subtype(INTERIM).build()),
            Arguments.of("Final care order", builder().type(CARE_ORDER).subtype(FINAL).build()),
            Arguments.of("Interim supervision order",
                builder().type(SUPERVISION_ORDER).subtype(INTERIM).build()),
            Arguments.of("Final supervision order", builder().type(SUPERVISION_ORDER).subtype(FINAL).build()),
            Arguments.of("Emergency protection order", builder().type(EMERGENCY_PROTECTION_ORDER).build()),
            Arguments.of("Discharge of care order", builder().type(DISCHARGE_OF_CARE_ORDER).build()),
            Arguments.of("Upload", builder().type(UPLOAD).build()),
            Arguments.of("Upload something blah", builder().type(UPLOAD).build())
        );
    }
}
