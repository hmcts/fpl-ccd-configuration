package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;

class ColleagueTest {

    @Test
    void shouldGetTitleForOther() {

        final Colleague colleague = Colleague.builder()
            .role(OTHER)
            .title("Test title")
            .build();

        assertThat(colleague.getJobTitle()).isEqualTo("Test title");
    }

    @Test
    void shouldGetNullForOtherWhenTitleNotPresent() {

        final Colleague colleague = Colleague.builder()
            .role(OTHER)
            .build();

        assertThat(colleague.getJobTitle()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ColleagueRole.class, names = {"OTHER"}, mode = EXCLUDE)
    void shouldGetRoleNameAsTitleWhenTitleDoesNotExists(ColleagueRole role) {

        final Colleague colleague = Colleague.builder()
            .role(role)
            .build();

        assertThat(colleague.getJobTitle()).isEqualTo(role.getLabel());
    }

    @ParameterizedTest
    @EnumSource(value = ColleagueRole.class, names = {"OTHER"}, mode = EXCLUDE)
    void shouldGetRoleNameWhenTitleExists(ColleagueRole role) {

        final Colleague colleague = Colleague.builder()
            .role(role)
            .build();

        assertThat(colleague.getJobTitle()).isEqualTo(role.getLabel());
    }
}
