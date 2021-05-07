package uk.gov.hmcts.reform.ccd.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeOrganisationRequestTest {

    @Test
    void shouldReturnNullIfCaseRoleIsNotPresent() {
        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder().build();

        assertThat(changeRequest.getCaseRole()).isNull();
    }

    @ParameterizedTest
    @EnumSource(SolicitorRole.class)
    void shouldExtractSolicitorRoleFromDynamicList(SolicitorRole solicitorRole) {

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(TestDataHelper.caseRoleDynamicList(solicitorRole))
            .build();

        assertThat(changeRequest.getCaseRole()).isEqualTo(solicitorRole);
    }

    @Test
    void shouldThrowExceptionWhenUnexpectedRole() {
        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(TestDataHelper.caseRoleDynamicList("[NOT_SOLICTITOR_ROLE]"))
            .build();

        assertThatThrownBy(changeRequest::getCaseRole).isInstanceOf(NoSuchElementException.class);
    }
}
