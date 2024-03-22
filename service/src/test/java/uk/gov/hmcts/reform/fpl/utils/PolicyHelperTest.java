package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.PolicyData;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyHelperTest {
    @Test
    void shouldReturnTrueIfPolicyIsMatchingCaseRoles() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName())
            .build();

        assertThat(PolicyHelper.isPolicyMatchingCaseRoles(organisationPolicy, List.of(CaseRole.SOLICITORA))).isTrue();
    }

    @Test
    void shouldReturnFalseIfPolicyIsNotMatchingCaseRoles() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName())
            .build();

        assertThat(PolicyHelper.isPolicyMatchingCaseRoles(organisationPolicy, List.of(CaseRole.SOLICITORB,
            CaseRole.SOLICITORC, CaseRole.SOLICITORD))).isFalse();
    }

    @Test
    void shouldProcessFieldByPolicyDatasIfMatchingCaseRoles() {
        PolicyData policyData = RespondentPolicyData.builder()
            .respondentPolicy0(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.SOLICITORA.formattedName())
                .build())
            .respondentPolicy1(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.SOLICITORB.formattedName())
                .build())
            .respondentPolicy2(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.SOLICITORC.formattedName())
                .build())
            .respondentPolicy3(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(CaseRole.SOLICITORD.formattedName())
                .build())
            .build();

        List<String> actualResult = new ArrayList<>();

        PolicyHelper.processFieldByPolicyDatas(policyData,
            "TestingField",
            Set.of(CaseRole.SOLICITORA, CaseRole.SOLICITORC),
            actualResult::add);

        assertThat(actualResult).isEqualTo(List.of("TestingField0", "TestingField2"));
    }

    @Test
    void shouldNotProcessFieldIfPolicyDatasIsEmpty() {
        List<String> actualResult = new ArrayList<>();

        PolicyHelper.processFieldByPolicyDatas(null,
            "TestingField",
            Set.of(CaseRole.SOLICITORA, CaseRole.SOLICITORC),
            actualResult::add);

        assertThat(actualResult).isEmpty();
    }
}
