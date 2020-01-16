package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;

class OrderTypeAndDocumentTest {

    @ParameterizedTest
    @MethodSource("typeSource")
    void shouldReturnOnlyMainTypeWhenNoSubtypeProvided(GeneratedOrderType type, String answer) {
        OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder()
            .type(type)
            .build();

        assertThat(typeAndDoc.getFullType()).isEqualTo(answer);
    }

    @ParameterizedTest
    @MethodSource("typeAndSubtypeSource")
    void shouldReturnFullTypeWhenSubtypeProvided(GeneratedOrderType type,
                                                 GeneratedOrderSubtype subtype,
                                                 String answer) {
        OrderTypeAndDocument typeAndDoc = OrderTypeAndDocument.builder()
            .type(type)
            .subtype(subtype)
            .build();

        assertThat(typeAndDoc.getFullType(subtype)).isEqualTo(answer);
    }

    private static Stream<Arguments> typeSource() {
        return Stream.of(
            Arguments.of(BLANK_ORDER, "Blank order (C21)"),
            Arguments.of(CARE_ORDER, "Care order"),
            Arguments.of(SUPERVISION_ORDER, "Supervision order")
        );
    }

    private static Stream<Arguments> typeAndSubtypeSource() {
        return Stream.of(
            Arguments.of(CARE_ORDER, INTERIM, "Interim care order"),
            Arguments.of(CARE_ORDER, FINAL, "Final care order"),
            Arguments.of(SUPERVISION_ORDER, INTERIM, "Interim supervision order"),
            Arguments.of(SUPERVISION_ORDER, FINAL, "Final supervision order")
        );
    }

}
