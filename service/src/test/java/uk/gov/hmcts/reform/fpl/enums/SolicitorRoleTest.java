package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitorRoleTest {

    @ParameterizedTest
    @MethodSource("typeToEnumSource")
    void shouldConvertTypeStringToSolicitorRole(String label, SolicitorRole expectedSolicitorRole) {
        assertThat(SolicitorRole.from(label)).isPresent().hasValue(expectedSolicitorRole);
    }

    private static Stream<Arguments> typeToEnumSource() {
        return Stream.of(
            Arguments.of("[SOLICITORA]", SolicitorRole.SOLICITORA),
            Arguments.of("[SOLICITORB]", SolicitorRole.SOLICITORB),
            Arguments.of("[SOLICITORC]", SolicitorRole.SOLICITORC),
            Arguments.of("[SOLICITORD]", SolicitorRole.SOLICITORD),
            Arguments.of("[SOLICITORE]", SolicitorRole.SOLICITORE),
            Arguments.of("[SOLICITORF]", SolicitorRole.SOLICITORF),
            Arguments.of("[SOLICITORG]", SolicitorRole.SOLICITORG),
            Arguments.of("[SOLICITORH]", SolicitorRole.SOLICITORH),
            Arguments.of("[SOLICITORI]", SolicitorRole.SOLICITORI),
            Arguments.of("[SOLICITORJ]", SolicitorRole.SOLICITORJ)
        );
    }
}
