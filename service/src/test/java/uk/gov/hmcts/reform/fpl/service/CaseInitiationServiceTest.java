package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthorityName;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LAMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.MLA;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testCourt;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseInitiationServiceTest {

    private static final Long CASE_ID = 1000L;
    private static final String USER_ID = "USER1";
    private static final String EXTERNAL_ORG_ID = "EXT001";
    private static final String EXTERNAL_ORG_NAME = "Private solicitor";
    private static final TestLocalAuthority LA1 = new TestLocalAuthority("SA", "Swansea City Council", "SA002");
    private static final TestLocalAuthority LA2 = new TestLocalAuthority("SN", "Swindon Borough Council", "SN002");

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

    @Mock
    private HmctsCourtLookupConfiguration courtLookup;

    @Spy
    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    @InjectMocks
    private CaseInitiationService underTest;

    @Nested
    class CaseOutsourcingType {

        String organisationId = randomAlphanumeric(10);

        @Test
        void shouldReturnEmptyOutsourcingTypeWhenOrganisationNotAllowedToRepresentAnyLocalAuthority() {
            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, EPS))
                .willReturn(emptyList());

            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, MLA))
                .willReturn(emptyList());

            assertThat(underTest.getOutsourcingType(organisationId)).isEmpty();
        }

        @Test
        void shouldReturnEPSOutsourcingTypeWhenOrganisationIsAllowedToRepresentLocalAuthorityAsExternalSolicitor() {
            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, EPS))
                .willReturn(List.of(LocalAuthorityName.builder().build()));

            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, MLA))
                .willReturn(emptyList());

            assertThat(underTest.getOutsourcingType(organisationId)).contains(EPS);
        }

        @Test
        void shouldReturnMLAOutsourcingTypeWhenOrganisationIsAllowedToRepresentLocalAuthorityAsLocalAuthority() {
            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, EPS))
                .willReturn(emptyList());

            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, MLA))
                .willReturn(List.of(LocalAuthorityName.builder().build()));

            assertThat(underTest.getOutsourcingType(organisationId)).contains(MLA);
        }

        @Test
        void shouldThrowExceptionWhenOrganisationConfiguredAsExternalSolicitorAnLocalAuthority() {
            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, EPS))
                .willReturn(List.of(LocalAuthorityName.builder().build()));

            given(localAuthorityService.getOutsourcingLocalAuthorities(organisationId, MLA))
                .willReturn(List.of(LocalAuthorityName.builder().build()));

            assertThatThrownBy(() -> underTest.getOutsourcingType(organisationId))
                .hasMessage(format("Organisation %s is configured as both EPS and MLA", organisationId));
        }
    }

    @Nested
    class OutsourcingLocalAuthorities {

        @Test
        void shouldReturnEmptyWhenUserOrganisationNotAllowedToCreateCaseOnBehalfOfLocalAuthorities() {
            given(localAuthorityService.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID, EPS)).willReturn(emptyList());

            DynamicList localAuthorities = underTest.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID, EPS);

            assertThat(localAuthorities).isEqualTo(DynamicList.builder()
                .value(DynamicListElement.EMPTY)
                .listItems(emptyList())
                .build());
        }

        @Test
        void shouldReturnListOfSortedLocalAuthoritiesWhenUserIsAllowedToCreateCaseOnTheirBehalf() {
            givenUserInOrganisation(EXTERNAL_ORG_ID);

            givenUserNotInLocalAuthority();

            given(localAuthorityService.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID, EPS)).willReturn(List.of(
                LocalAuthorityName.builder().name("LA 2").code("LA2").build(),
                LocalAuthorityName.builder().name("LA 1").code("LA1").build()
            ));

            DynamicList localAuthorities = underTest.getOutsourcingLocalAuthorities(EXTERNAL_ORG_ID, EPS);

            assertThat(localAuthorities).isEqualTo(
                DynamicList.builder()
                    .value(DynamicListElement.EMPTY)
                    .listItems(List.of(
                        DynamicListElement.builder().code("LA1").label("LA 1").build(),
                        DynamicListElement.builder().code("LA2").label("LA 2").build()))
                    .build());
        }

        @Test
        void shouldReturnListOfLocalAuthoritiesWithPreselectedCurrentUserLocalAuthority() {

            final TestLocalAuthority userLocalAuthority = LA1;

            givenUserInOrganisation(userLocalAuthority.orgId);

            givenUserInLocalAuthority(userLocalAuthority);

            given(localAuthorityService.getOutsourcingLocalAuthorities(userLocalAuthority.orgId, MLA)).willReturn(
                List.of(
                    LocalAuthorityName.builder().name("LA 2").code("LA2").build(),
                    LocalAuthorityName.builder().name("LA 1").code("LA1").build())
            );

            DynamicList localAuthorities = underTest.getOutsourcingLocalAuthorities(userLocalAuthority.orgId, MLA);

            assertThat(localAuthorities).isEqualTo(
                DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(userLocalAuthority.code)
                        .label(userLocalAuthority.name)
                        .build())
                    .listItems(List.of(
                        DynamicListElement.builder()
                            .code(userLocalAuthority.code)
                            .label(userLocalAuthority.name)
                            .build(),
                        DynamicListElement.builder()
                            .code("LA1")
                            .label("LA 1")
                            .build(),
                        DynamicListElement.builder()
                            .code("LA2")
                            .label("LA 2")
                            .build()))
                    .build());
        }

    }

    @Nested
    class UpdateOrganisationDetails {

        @Test
        void shouldUpdateCaseDataWhenLocalAuthorityUserCreatesCase() {
            final TestLocalAuthority userLocalAuthority = LA1;

            givenLocalAuthorityExists(userLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenUserInOrganisation(userLocalAuthority.orgId);

            CaseData caseData = givenCaseNotOutsourced();

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(userLocalAuthority.code);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(userLocalAuthority.name);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(organisationPolicy(userLocalAuthority.orgId, null, LASOLICITOR));

            assertThat(updatedCaseData.getOutsourcingPolicy())
                .isNull();
        }

        @Test
        void shouldUpdateCaseDataWhenLocalAuthorityUserSelectHisLocalAuthorityAsOutsourcingLocalAuthority() {
            final TestLocalAuthority userLocalAuthority = LA1;

            givenLocalAuthorityExists(userLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenUserInOrganisation(userLocalAuthority.orgId);

            CaseData caseData = givenCaseOutsourced(userLocalAuthority, MLA);

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(userLocalAuthority.code);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(userLocalAuthority.name);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(organisationPolicy(userLocalAuthority.orgId, null, LASOLICITOR));

            assertThat(updatedCaseData.getOutsourcingPolicy())
                .isNull();
        }

        @Test
        void shouldUpdateCaseDataWhenOutsourcedExternalSolicitorCreatesCase() {
            final TestLocalAuthority outsourcingLocalAuthority = LA1;

            givenLocalAuthorityExists(outsourcingLocalAuthority);
            givenUserInOrganisation(EXTERNAL_ORG_ID);

            CaseData caseData = givenCaseOutsourced(outsourcingLocalAuthority, EPS);

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(outsourcingLocalAuthority.code);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(outsourcingLocalAuthority.name);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(
                    organisationPolicy(outsourcingLocalAuthority.orgId, outsourcingLocalAuthority.name, LASOLICITOR));

            assertThat(updatedCaseData.getOutsourcingPolicy())
                .isEqualTo(organisationPolicy(EXTERNAL_ORG_ID, null, EPSMANAGING));
        }

        @Test
        void shouldUpdateCaseDataWhenOutsourcedLocalAuthoritySolicitorCreatesCase() {
            final TestLocalAuthority outsourcingLocalAuthority = LA1;
            final TestLocalAuthority userLocalAuthority = LA2;

            givenLocalAuthorityExists(userLocalAuthority);
            givenLocalAuthorityExists(outsourcingLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenUserInOrganisation(userLocalAuthority.orgId);

            CaseData caseData = givenCaseOutsourced(outsourcingLocalAuthority, MLA);

            CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCaseLocalAuthority())
                .isEqualTo(outsourcingLocalAuthority.code);

            assertThat(updatedCaseData.getCaseLocalAuthorityName())
                .isEqualTo(outsourcingLocalAuthority.name);

            assertThat(updatedCaseData.getLocalAuthorityPolicy())
                .isEqualTo(
                    organisationPolicy(outsourcingLocalAuthority.orgId, outsourcingLocalAuthority.name, LASOLICITOR));

            assertThat(updatedCaseData.getOutsourcingPolicy())
                .isEqualTo(
                    organisationPolicy(userLocalAuthority.orgId, null, LAMANAGING));
        }

        @Test
        void shouldAddDesignatedCourt() {
            final TestLocalAuthority userLocalAuthority = LA1;
            final Court court = testCourt();

            givenLocalAuthorityExists(userLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenLocalAuthorityCourts(court);

            final CaseData caseData = givenCaseNotOutsourced();

            final CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCourt()).isEqualTo(court);
            assertThat(updatedCaseData.getMultiCourts()).isNull();
        }

        @Test
        void shouldNotAddDesignatedCourtWhenMultipleCourtsAvailable() {
            final TestLocalAuthority userLocalAuthority = LA1;

            givenLocalAuthorityExists(userLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenLocalAuthorityCourts(testCourt(), testCourt());

            final CaseData caseData = givenCaseNotOutsourced();

            final CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCourt()).isNull();
            assertThat(updatedCaseData.getMultiCourts()).isEqualTo(YES);
        }

        @Test
        void shouldAddDesignatedCourtWhenCaseIsOutsourced() {
            final TestLocalAuthority outsourcingLocalAuthority = LA1;
            final TestLocalAuthority userLocalAuthority = LA2;
            final Court court = testCourt();

            givenLocalAuthorityExists(userLocalAuthority);
            givenLocalAuthorityExists(outsourcingLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenLocalAuthorityCourts(court);

            final CaseData caseData = givenCaseOutsourced(outsourcingLocalAuthority, MLA);

            final CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCourt()).isEqualTo(court);
            assertThat(updatedCaseData.getMultiCourts()).isNull();
        }

        @Test
        void shouldNotAddDesignatedCourtWhenCaseIsOutsourcedAndMultipleCourtsAvailable() {
            final TestLocalAuthority outsourcingLocalAuthority = LA1;
            final TestLocalAuthority userLocalAuthority = LA2;

            givenLocalAuthorityExists(userLocalAuthority);
            givenLocalAuthorityExists(outsourcingLocalAuthority);
            givenUserInLocalAuthority(userLocalAuthority);
            givenLocalAuthorityCourts(testCourt(), testCourt());

            final CaseData caseData = givenCaseOutsourced(outsourcingLocalAuthority, MLA);

            final CaseData updatedCaseData = underTest.updateOrganisationsDetails(caseData);

            assertThat(updatedCaseData.getCourt()).isNull();
            assertThat(updatedCaseData.getMultiCourts()).isEqualTo(YES);
        }
    }

    @Nested
    class CaseAccess {

        @BeforeEach
        void init() {
            given(requestData.userId()).willReturn(USER_ID);
        }

        @Test
        void shouldGrantCaseAccessToLocalAuthorityWhenCaseCreatedByLocalAuthorityUser() {
            final CaseRole caseRole = LASOLICITOR;

            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA1.code)
                .localAuthorityPolicy(organisationPolicy(LA1.orgId, LA1.name, caseRole))
                .build();

            underTest.grantCaseAccess(caseData);

            verify(caseRoleService).revokeCaseRoleFromUser(CASE_ID, USER_ID, CREATOR);
            verify(caseRoleService).grantCaseRoleToLocalAuthority(CASE_ID, USER_ID, LA1.code, caseRole);
            verifyNoMoreInteractions(caseRoleService);
        }

        @ParameterizedTest
        @EnumSource(OutsourcingType.class)
        void shouldGrantCaseAccessToCreatorOnlyWhenCaseIsOutsourced(OutsourcingType outsourcingType) {
            final CaseRole caseRole = outsourcingType.getCaseRole();

            given(requestData.userId()).willReturn(USER_ID);

            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA1.code)
                .localAuthorityPolicy(organisationPolicy(LA1.orgId, LA1.name, LASOLICITOR))
                .outsourcingPolicy(organisationPolicy(EXTERNAL_ORG_ID, EXTERNAL_ORG_NAME, caseRole))
                .build();

            underTest.grantCaseAccess(caseData);

            verify(caseRoleService).revokeCaseRoleFromUser(CASE_ID, USER_ID, CREATOR);
            verify(caseRoleService).grantCaseRoleToUser(CASE_ID, USER_ID, caseRole);
            verifyNoMoreInteractions(caseRoleService);
        }

    }

    @Nested
    class UserValidation {

        @Test
        void shouldReturnErrorWhenUserInOrganisationButNotInLocalAuthorityAndNotOutsourcingCase() {
            givenUserInOrganisation(EXTERNAL_ORG_ID);
            givenUserNotInLocalAuthority();

            CaseData caseData = givenCaseNotOutsourced();

            List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).containsExactly(
                "Email not recognised.",
                "Your email is not associated with a local authority or authorised legal firm.",
                "Email MyHMCTSsupport@justice.gov.uk for further guidance.");
        }

        @Test
        void shouldReturnErrorsWhenToggleIsDisabledAndUserIsInLocalAuthorityButNotInOrganisation() {
            givenUserNotInOrganisation();
            givenUserInLocalAuthority(LA1);
            givenCaseCreationAllowedForNonOnboardedUsers(false);

            CaseData caseData = givenCaseNotOutsourced();

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).containsExactly(
                "Register for an account.",
                "You cannot start an online application until you’re fully registered.",
                "Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                    + "for help with registration.");
        }

        @Test
        void shouldReturnErrorsWhenToggleIsDisabledAndUserIsNotInLocalAuthorityNorInOrganisation() {
            givenUserNotInOrganisation();
            givenUserNotInLocalAuthority();
            givenCaseCreationAllowedForNonOnboardedUsers(false);

            CaseData caseData = givenCaseNotOutsourced();

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).containsExactly(
                "Register for an account.",
                "You cannot start an online application until you’re fully registered "
                    + "and have permission to start a case for a local authority.",
                "Email MyHMCTSsupport@justice.gov.uk for further guidance.");
        }

        @Test
        void shouldReturnErrorsWhenToggleIsDisabledAndOutsourcingUserIsNotInOrganisation() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserNotInLocalAuthority();
            givenUserNotInOrganisation();

            CaseData caseData = givenCaseOutsourced(LA1, EPS);

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            verify(organisationService).findOrganisation();
            assertThat(errors).containsExactly(
                "Register for an account.",
                "You cannot start an online application until you’re fully registered.",
                "Email MyHMCTSsupport@justice.gov.uk for help with registration.");
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsDisabledAndUserIsInLocalAuthorityAndOrganisation() {
            givenUserInLocalAuthority(LA1);
            givenUserInOrganisation(LA1.orgId);
            givenCaseCreationAllowedForNonOnboardedUsers(false);

            CaseData caseData = givenCaseNotOutsourced();

            List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsEnabledAndUserIsInLocalAuthorityButNotInOrganisation() {
            givenUserNotInOrganisation();
            givenUserInLocalAuthority(LA1);
            givenCaseCreationAllowedForNonOnboardedUsers(true);

            CaseData caseData = givenCaseNotOutsourced();

            List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsDisabledAndUserFromOrganisationOutsourceCase() {
            givenCaseCreationAllowedForNonOnboardedUsers(false);
            givenUserInOrganisation(EXTERNAL_ORG_ID);
            givenUserNotInLocalAuthority();

            CaseData caseData = givenCaseOutsourced(LA1, EPS);

            final List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldNotReturnErrorsWhenToggleIsEnabledAndOutsourcedUserIsNotInLocalAuthorityNorOrganisation() {
            givenCaseCreationAllowedForNonOnboardedUsers(true);
            givenUserNotInLocalAuthority();
            givenUserNotInOrganisation();

            CaseData caseData = givenCaseOutsourced(LA1, EPS);

            List<String> errors = underTest.checkUserAllowedToCreateCase(caseData);

            assertThat(errors).isEmpty();
        }
    }

    private void givenLocalAuthorityCourts(Court... courts) {
        given(courtLookup.getCourt(any())).willReturn(Arrays.asList(courts));
    }

    private void givenUserInOrganisation(String organisationId) {
        Optional<Organisation> organisation = Optional.ofNullable(organisationId)
            .map(orgId -> Organisation.builder().organisationIdentifier(orgId).build());
        given(organisationService.findOrganisation()).willReturn(organisation);
    }

    private void givenUserNotInOrganisation() {
        given(organisationService.findOrganisation()).willReturn(Optional.empty());
    }

    private void givenUserInLocalAuthority(TestLocalAuthority localAuthority) {
        given(localAuthorityService.getLocalAuthorityCode()).willReturn(Optional.of(localAuthority.code));
        given(localAuthorityService.getUserLocalAuthority()).willReturn(Optional.of(LocalAuthorityName.builder()
            .name(localAuthority.name)
            .code(localAuthority.code)
            .build()));
    }

    private void givenUserNotInLocalAuthority() {
        given(localAuthorityService.getLocalAuthorityCode()).willReturn(Optional.empty());
        given(localAuthorityService.getUserLocalAuthority()).willReturn(Optional.empty());
    }

    private void givenLocalAuthorityExists(TestLocalAuthority localAuthority) {
        given(localAuthorityService.getLocalAuthorityId(localAuthority.code)).willReturn(localAuthority.orgId);
        given(localAuthorityService.getLocalAuthorityName(localAuthority.code)).willReturn(localAuthority.name);
    }

    private void givenCaseCreationAllowedForNonOnboardedUsers(boolean allowed) {
        given(featureToggleService.isCaseCreationForNotOnboardedUsersEnabled(any())).willReturn(allowed);
    }

    private CaseData givenCaseOutsourced(TestLocalAuthority localAuthority, OutsourcingType outsourcingType) {
        return CaseData.builder()
            .outsourcingLAs(localAuthority.code)
            .outsourcingType(outsourcingType)
            .build();
    }

    private CaseData givenCaseNotOutsourced() {
        return CaseData.builder()
            .outsourcingLAs(null)
            .build();
    }

    private OrganisationPolicy organisationPolicy(String organisationId, String organisationName, CaseRole caseRole) {
        return OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(organisationId)
                .organisationName(organisationName)
                .build())
            .orgPolicyCaseAssignedRole(caseRole.formattedName())
            .build();
    }

    static class TestLocalAuthority {
        String code;
        String name;
        String orgId;

        TestLocalAuthority(String code, String name, String orgId) {
            this.code = code;
            this.name = name;
            this.orgId = orgId;
        }
    }

}
