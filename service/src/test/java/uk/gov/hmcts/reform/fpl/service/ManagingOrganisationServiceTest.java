package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.exceptions.CaseNotOutsourcedException;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagingOrganisationServiceTest {

    private static final String ORGANISATION_ID = "ORG1";

    @Mock
    private Time time;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ManagingOrganisationService underTest;

    @Test
    void shouldReturnManagingOrganisation() {
        CaseData caseData = caseData(organisationPolicy(ORGANISATION_ID, "test", EPSMANAGING));

        Organisation expectedOrganisation = Organisation.builder()
            .organisationIdentifier(ORGANISATION_ID)
            .build();

        when(organisationService.findOrganisation(ORGANISATION_ID)).thenReturn(Optional.of(expectedOrganisation));

        Organisation actualOrganisation = underTest.getManagingOrganisation(caseData);

        assertThat(actualOrganisation).isEqualTo(expectedOrganisation);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("incompleteOrganisationPolicy")
    void shouldThrowExceptionWhenGettingManagingOrganisationAndCaseNotOutsourced(OrganisationPolicy outsourcingPolicy) {
        CaseData caseData = caseData(outsourcingPolicy);

        assertThatThrownBy(() -> underTest.getManagingOrganisation(caseData))
            .isInstanceOf(CaseNotOutsourcedException.class)
            .hasMessage("Case 10 is not outsourced");
    }

    @Test
    void shouldThrowExceptionWhenGettingManagingOrganisationAndOrganisationDoesNotExist() {
        CaseData caseData = caseData(organisationPolicy(ORGANISATION_ID, "test", EPSMANAGING));

        when(organisationService.findOrganisation(ORGANISATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getManagingOrganisation(caseData))
            .isInstanceOf(OrganisationNotFound.class)
            .hasMessage("Organisation ORG1 not found");
    }

    @Test
    void shouldGetRemovalRequest() {
        OrganisationPolicy outsourcingPolicy = organisationPolicy(ORGANISATION_ID, "test", EPSMANAGING);

        CaseData caseData = caseData(outsourcingPolicy);

        ChangeOrganisationRequest actualChangeOrganisationRequest = underTest.getRemovalRequest(caseData);

        ChangeOrganisationRequest expectedChangeOrganisationRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(caseRoleDynamicList(outsourcingPolicy.getOrgPolicyCaseAssignedRole()))
            .organisationToRemove(outsourcingPolicy.getOrganisation())
            .build();

        assertThat(actualChangeOrganisationRequest).isEqualTo(expectedChangeOrganisationRequest);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("incompleteOrganisationPolicy")
    void shouldThrowExceptionWhenGettingRemovalRequestAndCaseIsNotOutsourced(OrganisationPolicy outsourcingPolicy) {
        CaseData caseData = caseData(outsourcingPolicy);

        assertThatThrownBy(() -> underTest.getRemovalRequest(caseData))
            .isInstanceOf(CaseNotOutsourcedException.class)
            .hasMessage("Case 10 is not outsourced");
    }

    private static Stream<OrganisationPolicy> incompleteOrganisationPolicy() {
        return Stream.of(
            OrganisationPolicy.builder().build(),
            OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                .build(),
            OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("").build())
                .build()
        );
    }

    private static CaseData caseData(OrganisationPolicy outsourcingPolicy) {
        return CaseData.builder()
            .id(10L)
            .outsourcingPolicy(outsourcingPolicy)
            .build();
    }
}
