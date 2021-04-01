package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseRoleTest {

    @ParameterizedTest
    @EnumSource(CaseRole.class)
    void shouldCreateCaseRoleFromFormattedName(CaseRole caseRole) {
        CaseRole parsedCaseRole = CaseRole.from(caseRole.formattedName());

        assertThat(parsedCaseRole).isEqualTo(caseRole);
    }

    @ParameterizedTest
    @EnumSource(CaseRole.class)
    void shouldCreateCaseRoleFromName(CaseRole caseRole) {
        CaseRole parsedCaseRole = CaseRole.from(caseRole.name());

        assertThat(parsedCaseRole).isEqualTo(caseRole);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "aa"})
    void shouldRethrowExceptionWhenStringIsValid(String s) {
        assertThatThrownBy(() -> CaseRole.from(s))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
