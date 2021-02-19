package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseInitiationServiceTest {

    private static final Long CASE_ID = 1000L;
    private static final String USER_ID = "USER1";
    private static final String EXTERNAL_ORG_ID = "EXT001";
    private static final String EXTERNAL_ORG_NAME = "Private solicitor";
    private static final String LOCAL_AUTHORITY_ORG_ID = "SA002";
    private static final String LOCAL_AUTHORITY_CODE = "SA";
    private static final String LOCAL_AUTHORITY_NAME = "Swansea City Council";

    @Mock
    private RequestData requestData;

    @Mock
    private CaseAccessService caseRoleService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocalAuthorityService localAuthorityService;

    @Spy
    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    @InjectMocks
    private CaseInitiationService underTest;

    @Nested
    class OutsourcingLocalAuthorities {

        @Test
        void shouldReturnEmptyWhenUserNotOnboarded() {
            givenUserFromOrganisation(null);

            Optional<DynamicList> list = underTest.getOutsourcingLocalAuthoritiesDynamicList();
            assertThat(list).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenUserOrganisationNotAllowedToCreateCaseOnBehalfOfLocalAuthorities() {
            givenUserFromOrganisation(EXTERNAL_ORG_ID);

            given(localAuthorityService.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID)).willReturn(emptyList());

            Optional<DynamicList> localAuthoritiesDynamicList = underTest.getOutsourcingLocalAuthoritiesDynamicList();

            assertThat(localAuthoritiesDynamicList).isEmpty();

        }

        @Test
        void shouldReturnListOfSortedLocalAuthoritiesWhenUserIsAllowedToCreateCaseOnTheirBehalf() {
            givenUserFromOrganisation(EXTERNAL_ORG_ID);

            given(localAuthorityService.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID)).willReturn(List.of(
                LocalAuthority.builder().name("LA 2").code("LA2").build(),
                LocalAuthority.builder().name("LA 1").code("LA1").build()
            ));

            Optional<DynamicList> localAuthoritiesDynamicList = underTest.getOutsourcingLocalAuthoritiesDynamicList();

            assertThat(localAuthoritiesDynamicList).contains(
                DynamicList.builder()
                    .value(DynamicListElement.EMPTY)
                    .listItems(List.of(
                        DynamicListElement.builder().code("LA1").label("LA 1").build(),
                        DynamicListElement.builder().code("LA2").label("LA 2").build()))
                    .build());
        }

    }

    @Nested
    class UpdateOrganisationDetails {

        @Test
        void shouldUpdateCaseDataWhenLocalAuthorityUserCreatesCase() {
            givenUserFromOrganisation(LOCAL_AUTHORITY_ORG_ID);
            given(localAuthorityService.getLocalAuthorityCode()).willReturn(LOCAL_AUTHORITY_CODE);
            given(localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_NAME);

            CaseData caseData = CaseData.builder().build();

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(LOCAL_AUTHORITY_CODE);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(LOCAL_AUTHORITY_NAME);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(organisationPolicy(LOCAL_AUTHORITY_ORG_ID, null, "[LASOLICITOR]"));

            assertThat(updatedCaseData.getOutsourcingPolicy()).isNull();
        }

        @Test
        void shouldUpdateCaseDataWhenOutsourcedUserCreatesCase() {
            Organisation outsourcedOrganisation = Organisation.builder()
                .organisationIdentifier(EXTERNAL_ORG_ID)
                .name(EXTERNAL_ORG_NAME)
                .build();

            given(organisationService.findOrganisation()).willReturn(Optional.of(outsourcedOrganisation));

            given(localAuthorityService.getLocalAuthorityId(LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_ORG_ID);
            given(localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_NAME);

            CaseData caseData = CaseData.builder()
                .outsourcingLAs(LOCAL_AUTHORITY_CODE)
                .build();

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(LOCAL_AUTHORITY_CODE);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(LOCAL_AUTHORITY_NAME);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(organisationPolicy(LOCAL_AUTHORITY_ORG_ID, LOCAL_AUTHORITY_NAME,"[LASOLICITOR]"));

            assertThat(updatedCaseData.getOutsourcingPolicy())
                .isEqualTo(organisationPolicy(EXTERNAL_ORG_ID, EXTERNAL_ORG_NAME,"[EPSMANAGING]"));
        }
    }

    @Nested
    class CaseAccess {

        @Test
        void shouldGrantCaseAccessToLocalAuthorityWhenCaseCreatedByLocalAuthorityUser() {
            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .build();

            underTest.grantCaseAccess(caseData);

            verify(caseRoleService)
                .grantCaseRoleToLocalAuthority(CASE_ID, LOCAL_AUTHORITY_CODE, LASOLICITOR);
        }

        @Test
        void shouldGrantCaseAccessToCreatorOnlyWhenCaseCreatedByOutsourcedUser() {
            given(requestData.userId()).willReturn(USER_ID);

            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .outsourcingPolicy(organisationPolicy(EXTERNAL_ORG_ID, EXTERNAL_ORG_NAME,"[EPSMANAGING]"))
                .build();

            underTest.grantCaseAccess(caseData);

            verify(caseRoleService).revokeCaseRoleFromUser(CASE_ID, USER_ID, CREATOR);
            verify(caseRoleService).grantCaseRoleToUser(CASE_ID, USER_ID, EPSMANAGING);
        }

    }

    @Nested
    class UserValidation {

        @BeforeEach
        void init() {
            given(localAuthorityService.getLocalAuthorityCode()).willReturn(LOCAL_AUTHORITY_CODE);
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsDisabledAndUserIsOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserFromOrganisation(LOCAL_AUTHORITY_ORG_ID);

            List<String> errors = underTest.checkUserAllowedToCreateCase(CaseData.builder().build());

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrorsWhenToggleIsDisabledAndUserHasNotBeenOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserFromOrganisation(null);

            final List<String> errors = underTest.checkUserAllowedToCreateCase(CaseData.builder().build());

            assertThat(errors).containsExactly(
                "Register for an account.",
                "You cannot start an online application until you’re fully registered.",
                "Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                    + "for help with registration.");
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsEnabledAndUserNotOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(true);
            givenUserFromOrganisation(null);

            List<String> errors = underTest.checkUserAllowedToCreateCase(CaseData.builder().build());

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsDisabledAndOutsourcedUserIsOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserFromOrganisation(EXTERNAL_ORG_ID);

            CaseData caseData = CaseData.builder()
                .outsourcingLAs(LOCAL_AUTHORITY_CODE)
                .build();

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrorsWhenToggleIsDisabledAndOutsourcedUserHasNotBeenOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserFromOrganisation(null);

            CaseData caseData = CaseData.builder()
                .outsourcingLAs(LOCAL_AUTHORITY_CODE)
                .build();

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            verify(organisationService).findOrganisation();
            assertThat(errors).containsExactly(
                "Register for an account.",
                "You cannot start an online application until you’re fully registered.",
                "Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                    + "for help with registration.");
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsEnabledAndOutsourceUserNotOnboarded() {
            givenCaseCreationAllowedForNonOnboardedUsers(true);
            givenUserFromOrganisation(null);

            CaseData caseData = CaseData.builder()
                .outsourcingLAs(LOCAL_AUTHORITY_CODE)
                .build();

            List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            verifyNoInteractions(organisationService, localAuthorityService);
            assertThat(errors.isEmpty());
        }
    }

    private void givenUserFromOrganisation(String organisationId) {
        Optional<Organisation> organisation = Optional.ofNullable(organisationId)
            .map(orgId -> Organisation.builder().organisationIdentifier(orgId).build());
        given(organisationService.findOrganisation()).willReturn(organisation);
    }

    private OrganisationPolicy organisationPolicy(String organisationId, String organisationName, String caseRole) {
        return OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(organisationId)
                .organisationName(organisationName)
                .build())
            .orgPolicyCaseAssignedRole(caseRole)
            .build();
    }

    private void givenCaseCreationAllowedForNonOnboardedUsers(boolean allowed) {
        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(any())).willReturn(allowed);
    }

}
