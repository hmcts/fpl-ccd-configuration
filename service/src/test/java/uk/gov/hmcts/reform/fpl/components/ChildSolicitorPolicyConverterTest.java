package uk.gov.hmcts.reform.fpl.components;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ChildSolicitorPolicyConverterTest {

    private static final SolicitorRole SOLICITOR_ROLE = SolicitorRole.CHILDSOLICITORA;
    private static final Organisation ORGANISATION = Organisation.builder()
        .organisationID("ID")
        .organisationName("Name")
        .build();

    private final ChildSolicitorPolicyConverter underTest = new ChildSolicitorPolicyConverter();

    @Test
    void testChildNotPresent() {
        OrganisationPolicy actual = underTest.generate(SOLICITOR_ROLE, Optional.empty());

        assertThat(actual).isEqualTo(OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(SOLICITOR_ROLE.getCaseRoleLabel())
            .build()
        );
    }

    @Test
    void testRespondentNotPresent() {
        OrganisationPolicy actual = underTest.generate(SOLICITOR_ROLE, Optional.of(element(
            Child.builder()
                .representative(null)
                .build()
        )));

        assertThat(actual).isEqualTo(OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(SOLICITOR_ROLE.getCaseRoleLabel())
            .build()
        );
    }

    @Test
    void testRespondentOrganisationNotPresent() {
        OrganisationPolicy actual = underTest.generate(SOLICITOR_ROLE, Optional.of(element(
            Child.builder()
                .representative(RespondentSolicitor.builder().organisation(null).build())
                .build()
        )));

        assertThat(actual).isEqualTo(OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(SOLICITOR_ROLE.getCaseRoleLabel())
            .build()
        );
    }

    @Test
    void testRespondentOrganisationPresent() {
        OrganisationPolicy actual = underTest.generate(SOLICITOR_ROLE, Optional.of(element(
            Child.builder()
                .representative(RespondentSolicitor.builder().organisation(ORGANISATION).build())
                .build()
        )));

        assertThat(actual).isEqualTo(OrganisationPolicy.builder()
            .organisation(ORGANISATION)
            .orgPolicyCaseAssignedRole(SOLICITOR_ROLE.getCaseRoleLabel())
            .build()
        );
    }
}
