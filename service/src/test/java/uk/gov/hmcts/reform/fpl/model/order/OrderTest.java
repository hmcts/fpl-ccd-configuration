package uk.gov.hmcts.reform.fpl.model.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.REVIEW;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_2;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_3;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_4;

class OrderTest {

    @Test
    void fileExtension() {
        assertThat(C32_CARE_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c32_care_order.pdf");
        assertThat(C32_CARE_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c32_care_order.doc");
    }

    @Test
    void firstSection() {
        assertThat(C32_CARE_ORDER.firstSection()).isEqualTo(SECTION_2);
    }

    @ParameterizedTest
    @MethodSource("sectionsWithNext")
    void testNextSection(Order order, OrderSection currentSection, Optional<OrderSection> expectedNextSection) {
        assertThat(order.nextSection(currentSection)).isEqualTo(expectedNextSection);
    }

    private static Stream<Arguments> sectionsWithNext() {
        return Stream.of(
            Arguments.of(C32_CARE_ORDER, SECTION_2, Optional.of(SECTION_3)),
            Arguments.of(C32_CARE_ORDER, SECTION_3, Optional.of(SECTION_4)),
            Arguments.of(C32_CARE_ORDER, SECTION_4, Optional.of(REVIEW)),
            Arguments.of(C32_CARE_ORDER, REVIEW, Optional.empty())
        );
    }
}
