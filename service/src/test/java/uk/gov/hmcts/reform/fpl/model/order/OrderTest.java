package uk.gov.hmcts.reform.fpl.model.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.CHILDREN_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ISSUING_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ORDER_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.REVIEW;

class OrderTest {

    @Test
    void fileExtension() {
        assertThat(C32_CARE_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c32_care_order.pdf");
        assertThat(C32_CARE_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c32_care_order.doc");
    }

    @Test
    void firstSection() {
        assertThat(C32_CARE_ORDER.firstSection()).isEqualTo(ISSUING_DETAILS);
    }

    @ParameterizedTest
    @MethodSource("sectionsWithNext")
    void testNextSection(Order order, OrderSection currentSection, Optional<OrderSection> expectedNextSection) {
        assertThat(order.nextSection(currentSection)).isEqualTo(expectedNextSection);
    }

    private static Stream<Arguments> sectionsWithNext() {
        return Stream.of(
            Arguments.of(C32_CARE_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C32_CARE_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C32_CARE_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C32_CARE_ORDER, REVIEW, Optional.empty()),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, REVIEW, Optional.empty())
        );
    }
}
