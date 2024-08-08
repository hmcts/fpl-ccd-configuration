package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationPolicyNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CourtRegion.LONDON;
import static uk.gov.hmcts.reform.fpl.enums.CourtRegion.MIDLANDS;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ManageLocalAuthoritiesServiceTest {

    @Spy
    private Time time = new FixedTimeConfiguration().stoppedTime();

    @Mock
    private ValidateEmailService emailService;

    @Mock
    private DynamicListService dynamicListService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private HmctsCourtLookupConfiguration courtLookup;

    @Mock
    private CourtLookUpService courtLookUpService;

    @Mock
    private CourtService courtService;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorities;

    @Mock
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmails;

    @Mock
    private LocalAuthorityIdLookupConfiguration localAuthorityIds;

    @Mock
    private ApplicantLocalAuthorityService localAuthorityService;

    @InjectMocks
    private ManageLocalAuthoritiesService underTest;

    @Nested
    class LocalAuthoritiesAction {

        @Test
        void shouldReturnLocalAuthorityActionForCourtAdminRole() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(null)
                .localAuthorityAction(ADD)
                .localAuthorityActionLA(null)
                .build();

            final CaseData caseData = CaseData.builder().localAuthoritiesEventData(eventData).build();

            assertThat(underTest.getLocalAuthorityAction(caseData)).isEqualTo(ADD);
        }

        @Test
        void shouldReturnLocalAuthorityActionForSolicitorRole() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(YES)
                .localAuthorityAction(null)
                .localAuthorityActionLA(REMOVE)
                .build();

            final CaseData caseData = CaseData.builder().localAuthoritiesEventData(eventData).build();

            assertThat(underTest.getLocalAuthorityAction(caseData)).isEqualTo(REMOVE);
        }
    }

    @Nested
    class ValidateAction {

        @Test
        void shouldReturnErrorWhenUserTriesToAddAnotherSecondaryLocalAuthority() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(ADD)
                .build();

            final CaseData caseData = CaseData.builder()
                .sharedLocalAuthorityPolicy(organisationPolicy("ORG1", "ORG name", LASHARED))
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).containsExactly(
                "Case access has already been given to local authority. Remove their access to continue.");
        }

        @Test
        void shouldReturnErrorWhenUserTriesToRemoveNonExistingSecondaryLocalAuthority() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(REMOVE)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).containsExactly("There are no other local authorities to remove from this case");
        }


        @Test
        void shouldReturnNoErrorWhenUserTriesToAddFirstSecondaryLocalAuthority() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(ADD)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).isEmpty();
        }

        @Test
        void shouldReturnNoErrorWhenUserTriesToRemoveSecondaryLocalAuthority() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(REMOVE)
                .build();

            final CaseData caseData = CaseData.builder()
                .sharedLocalAuthorityPolicy(organisationPolicy("ORG1", "ORG name", LASHARED))
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).isEmpty();
        }

        @Test
        void shouldReturnEmptyErrorsWhenUserTriesToTransferCase() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).isEmpty();
        }
    }

    @Nested
    class LocalAuthoritiesToShare {

        @Test
        void shouldReturnListOfSortedLocalAuthoritiesExcludingDesignatedOne() {

            final Map<String, String> allLocalAuthorities = Map.of(
                "LA3", "Local authority 3",
                "LA2", "Local authority 2",
                "LA1", "Local authority 1");

            final Map<String, String> expectedLocalAuthorities = Map.of(
                "LA1", "Local authority 1",
                "LA3", "Local authority 3");

            final DynamicList expectedLocalAuthoritiesList = dynamicList(expectedLocalAuthorities);

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA2")
                .build();

            when(localAuthorities.getLocalAuthoritiesNames()).thenReturn(allLocalAuthorities);
            when(dynamicListService.asDynamicList(expectedLocalAuthorities)).thenReturn(expectedLocalAuthoritiesList);

            final DynamicList actualLocalAuthorities = underTest.getLocalAuthoritiesToShare(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedLocalAuthoritiesList);

            verify(dynamicListService).asDynamicList(expectedLocalAuthorities);
        }

        @Test
        void shouldReturnListOfSortedLocalAuthorities() {

            final Map<String, String> allLocalAuthorities = Map.of(
                "LA3", "Local authority 3",
                "LA2", "Local authority 2",
                "LA1", "Local authority 1");

            final DynamicList expectedLocalAuthoritiesList = dynamicList(allLocalAuthorities);

            final CaseData caseData = CaseData.builder()
                .build();

            when(localAuthorities.getLocalAuthoritiesNames()).thenReturn(allLocalAuthorities);
            when(dynamicListService.asDynamicList(allLocalAuthorities)).thenReturn(expectedLocalAuthoritiesList);

            final DynamicList actualLocalAuthorities = underTest.getLocalAuthoritiesToShare(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedLocalAuthoritiesList);

            verify(dynamicListService).asDynamicList(allLocalAuthorities);
        }

    }

    @Nested
    class LocalAuthoritiesToTransfer {

        @Test
        void shouldReturnListOfSortedLocalAuthoritiesExcludingDesignatedAndSecondaryOnes() {

            final Map<String, String> allLocalAuthorities = Map.of(
                "LA3", "Local authority 3",
                "LA2", "Local authority 2",
                "LA4", "Local authority 4",
                "LA1", "Local authority 1");

            final Map<String, String> expectedLocalAuthorities = Map.of(
                "LA1", "Local authority 1",
                "LA4", "Local authority 4");

            final DynamicList expectedLocalAuthoritiesList = dynamicList(expectedLocalAuthorities);

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA2")
                .sharedLocalAuthorityPolicy(organisationPolicy("ORG3", "Org 3", LASHARED))
                .build();

            when(localAuthorityIds.getLocalAuthorityCode("ORG3")).thenReturn(Optional.of("LA3"));
            when(localAuthorities.getLocalAuthoritiesNames()).thenReturn(allLocalAuthorities);
            when(dynamicListService.asDynamicList(expectedLocalAuthorities)).thenReturn(expectedLocalAuthoritiesList);

            final DynamicList actualLocalAuthorities = underTest.getLocalAuthoritiesToTransfer(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedLocalAuthoritiesList);

            verify(dynamicListService).asDynamicList(expectedLocalAuthorities);
        }

        @Test
        void shouldReturnListOfSortedLocalAuthoritiesExcludingDesignatedOne() {

            final Map<String, String> allLocalAuthorities = Map.of(
                "LA3", "Local authority 3",
                "LA2", "Local authority 2",
                "LA4", "Local authority 4",
                "LA1", "Local authority 1");

            final Map<String, String> expectedLocalAuthorities = Map.of(
                "LA1", "Local authority 1",
                "LA3", "Local authority 3",
                "LA4", "Local authority 4");

            final DynamicList expectedLocalAuthoritiesList = dynamicList(expectedLocalAuthorities);

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA2")
                .build();

            when(localAuthorities.getLocalAuthoritiesNames()).thenReturn(allLocalAuthorities);
            when(dynamicListService.asDynamicList(expectedLocalAuthorities)).thenReturn(expectedLocalAuthoritiesList);

            final DynamicList actualLocalAuthorities = underTest.getLocalAuthoritiesToTransfer(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedLocalAuthoritiesList);

            verify(dynamicListService).asDynamicList(expectedLocalAuthorities);
        }

    }

    @Nested
    class LocalAuthorityToTransferDetails {

        private final String localAuthorityOrgId = "ORG2";
        private final String localAuthorityName = "Organisation 2";
        private final String localAuthorityCode = "LA2";
        private final String localAuthorityEmail = "la2@test.com";

        @Test
        void shouldPopulateLocalAuthority() {

            final uk.gov.hmcts.reform.rd.model.Organisation localAuthorityOrganisation =
                uk.gov.hmcts.reform.rd.model.Organisation.builder()
                    .organisationIdentifier(localAuthorityOrgId)
                    .name(localAuthorityName)
                    .build();

            final LocalAuthority localAuthorityFromOrganisation = LocalAuthority.builder()
                .id(localAuthorityOrganisation.getOrganisationIdentifier())
                .name(localAuthorityOrganisation.getName())
                .build();

            final Colleague expectedDefaultSolicitor = Colleague.builder()
                .role(SOLICITOR)
                .notificationRecipient(YES.getValue())
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(localAuthorityOrganisation.getOrganisationIdentifier())
                .name(localAuthorityOrganisation.getName())
                .email(localAuthorityEmail)
                .colleagues(wrapElements(expectedDefaultSolicitor))
                .build();

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                localAuthorityCode, localAuthorityOrganisation.getName()
            ), localAuthorityCode);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToTransfer(localAuthoritiesList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            when(localAuthorityIds.getLocalAuthorityId(localAuthorityCode))
                .thenReturn(localAuthorityOrganisation.getOrganisationIdentifier());

            when(localAuthorityService.findLocalAuthority(caseData, localAuthorityOrgId))
                .thenReturn(Optional.empty());

            when(organisationService.getOrganisation(localAuthorityOrgId))
                .thenReturn(localAuthorityOrganisation);

            when(localAuthorityService.getLocalAuthority(localAuthorityOrganisation))
                .thenReturn(localAuthorityFromOrganisation);

            when(localAuthorityEmails.getSharedInbox(localAuthorityCode))
                .thenReturn(Optional.of(localAuthorityEmail));

            final LocalAuthority actualLocalAuthority = underTest.getLocalAuthorityToTransferDetails(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetExistingLocalAuthorityWithSolicitor() {

            final Colleague expectedDefaultSolicitor = Colleague.builder()
                .role(SOLICITOR)
                .fullName("Emma Green")
                .email("emma.green@test.com")
                .notificationRecipient(YES.getValue())
                .build();

            final LocalAuthority existingLocalAuthority = LocalAuthority.builder()
                .id(localAuthorityOrgId)
                .name(localAuthorityName)
                .email(localAuthorityEmail)
                .colleagues(wrapElements(expectedDefaultSolicitor))
                .build();

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                localAuthorityCode, localAuthorityName
            ), localAuthorityCode);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToTransfer(localAuthoritiesList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            when(localAuthorityIds.getLocalAuthorityId(localAuthorityCode))
                .thenReturn(localAuthorityOrgId);

            when(localAuthorityService.findLocalAuthority(caseData, localAuthorityOrgId))
                .thenReturn(Optional.of(element(existingLocalAuthority)));

            final LocalAuthority actualLocalAuthority = underTest.getLocalAuthorityToTransferDetails(caseData);

            assertThat(actualLocalAuthority).isEqualTo(existingLocalAuthority);

            verifyNoInteractions(organisationService, localAuthorityEmails);
        }

        @Test
        void shouldGetExistingLocalAuthorityAndAddMissingSolicitor() {

            final Colleague expectedDefaultSolicitor = Colleague.builder()
                .role(SOLICITOR)
                .notificationRecipient(YES.getValue())
                .build();

            final LocalAuthority existingLocalAuthority = LocalAuthority.builder()
                .id(localAuthorityOrgId)
                .name(localAuthorityName)
                .email(localAuthorityEmail)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(localAuthorityOrgId)
                .name(localAuthorityName)
                .email(localAuthorityEmail)
                .colleagues(wrapElements(expectedDefaultSolicitor))
                .build();

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                localAuthorityCode, localAuthorityName
            ), localAuthorityCode);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToTransfer(localAuthoritiesList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            when(localAuthorityIds.getLocalAuthorityId(localAuthorityCode))
                .thenReturn(localAuthorityOrgId);

            when(localAuthorityService.findLocalAuthority(caseData, localAuthorityOrgId))
                .thenReturn(Optional.of(element(existingLocalAuthority)));

            final LocalAuthority actualLocalAuthority = underTest.getLocalAuthorityToTransferDetails(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);

            verifyNoInteractions(organisationService, localAuthorityEmails);
        }
    }

    @Nested
    class Transfer {

        private final String oldDesignatedLocalAuthorityCode = "LA1";
        private final String oldDesignatedLocalAuthorityOrgId = "ORG";
        private final String oldDesignatedAuthorityName = "Designated LA";
        private final String oldDesignatedAuthorityEmail = "la1@test.com";

        private final String newDesignatedLocalAuthorityCode = "LA2";
        private final String newDesignatedLocalAuthorityOrgId = "ORG2";
        private final String newDesignatedLocalAuthorityName = "Organisation 2";
        private final String newDesignatedLocalAuthorityEmail = "la2@test.com";

        private final Address address = testAddress();

        private final Organisation newDesignatedLocalAuthorityOrganisation = Organisation.builder()
            .organisationID(newDesignatedLocalAuthorityOrgId)
            .organisationName(newDesignatedLocalAuthorityName)
            .build();

        private final Colleague newSolicitor = Colleague.builder()
            .role(SOLICITOR)
            .fullName("Emma Green")
            .email("emma.green@test.com")
            .notificationRecipient(YES.getValue())
            .build();

        private final Colleague oldSolicitor = Colleague.builder()
            .role(SOLICITOR)
            .fullName("Gregory Black")
            .email("gregory.black@test.com")
            .phone("7777777777")
            .notificationRecipient(YES.getValue())
            .build();

        private final DynamicList courts = dynamicList(Map.of(
            "C1", "Court 1",
            "C2", "Court 2"),
            "C2");

        private final Court oldCourt = Court.builder()
            .code("C1")
            .name("Court 1")
            .build();

        private final Court newCourt = Court.builder()
            .code("C2")
            .name("Court 2")
            .build();

        final LocalAuthority oldDesignatedLocalAuthority = LocalAuthority.builder()
            .id(oldDesignatedLocalAuthorityOrgId)
            .name(oldDesignatedAuthorityName)
            .email(oldDesignatedAuthorityEmail)
            .designated("Yes")
            .build();

        @BeforeEach
        void init() {
            when(courtLookup.getCourtByCode("C1")).thenReturn(Optional.of(oldCourt));
            when(courtLookup.getCourtByCode("C2")).thenReturn(Optional.of(newCourt));
            when(courtLookUpService.getCourtByCode("C1")).thenReturn(Optional.of(oldCourt));
            when(courtLookUpService.getCourtByCode("C2")).thenReturn(Optional.of(newCourt));
            when(localAuthorityIds.getLocalAuthorityCode(newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.of(newDesignatedLocalAuthorityCode));
        }

        @Test
        void shouldTransferCaseToSecondaryLocalAuthorityWithCourtChange() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .courtsToTransfer(courts)
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id(newDesignatedLocalAuthorityOrgId)
                .name("Organisation 2 old")
                .email("org2old@test.com")
                .pbaNumber("PBA111")
                .colleagues(wrapElements(oldSolicitor))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority, sharedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.of(element(sharedLocalAuthority)));

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .pbaNumber(sharedLocalAuthority.getPbaNumber())
                .colleagues(wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .fullName(newSolicitor.getFullName())
                    .email(newSolicitor.getEmail())
                    .phone(oldSolicitor.getPhone())
                    .notificationRecipient(YES.getValue())
                    .build()))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).extracting("name", "code")
                .containsExactly("Family Court sitting at Court 2", "C2");
        }

        @Test
        void shouldTransferCaseToSecondaryLocalAuthorityWithoutCourtChange() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .transferToCourt(NO)
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id(newDesignatedLocalAuthorityOrgId)
                .name("Organisation 2 old")
                .email("org2old@test.com")
                .pbaNumber("PBA111")
                .colleagues(wrapElements(oldSolicitor))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority, sharedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.of(element(sharedLocalAuthority)));

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .pbaNumber(sharedLocalAuthority.getPbaNumber())
                .colleagues(wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .fullName(newSolicitor.getFullName())
                    .email(newSolicitor.getEmail())
                    .phone(oldSolicitor.getPhone())
                    .notificationRecipient(YES.getValue())
                    .build()))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).isEqualTo(oldCourt);
        }

        @Test
        void shouldTransferCaseToSecondaryLocalAuthorityAndAddRequiredSolicitorWhenNotPresent() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .transferToCourt(NO)
                .build();

            final Colleague nonSolicitor = Colleague.builder()
                .role(SOCIAL_WORKER)
                .mainContact("Yes")
                .fullName("John Wall")
                .email("john.wall@test.com")
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id(newDesignatedLocalAuthorityOrgId)
                .name("Organisation 2 old")
                .email("org2old@test.com")
                .pbaNumber("PBA111")
                .colleagues(wrapElements(nonSolicitor))
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority, sharedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.of(element(sharedLocalAuthority)));

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .pbaNumber(sharedLocalAuthority.getPbaNumber())
                .colleagues(wrapElements(nonSolicitor, newSolicitor))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).isEqualTo(oldCourt);
        }

        @Test
        void shouldTransferCaseToSecondaryLocalAuthorityAndAddMainContactSolicitorWhenNoOtherColleagues() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .transferToCourt(NO)
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id(newDesignatedLocalAuthorityOrgId)
                .name("Organisation 2 old")
                .email("org2old@test.com")
                .pbaNumber("PBA111")
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority, sharedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.of(element(sharedLocalAuthority)));

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .pbaNumber(sharedLocalAuthority.getPbaNumber())
                .colleagues(wrapElements(newSolicitor.toBuilder().mainContact(YES.getValue()).build()))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).isEqualTo(oldCourt);
        }

        @Test
        void shouldTransferCaseToNewLocalAuthorityWithCourtChange() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .courtsToTransfer(courts)
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.empty());

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .colleagues(wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .mainContact("Yes")
                    .fullName(newSolicitor.getFullName())
                    .email(newSolicitor.getEmail())
                    .notificationRecipient(YES.getValue())
                    .build()))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).extracting("name", "code")
                .containsExactly("Family Court sitting at Court 2", "C2");
        }

        @Test
        void shouldTransferCaseToNonSecondaryLocalAuthorityWithCourtChange() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(newDesignatedLocalAuthorityOrgId)
                    .name(newDesignatedLocalAuthorityName)
                    .email(newDesignatedLocalAuthorityEmail)
                    .address(address)
                    .build())
                .localAuthorityToTransferSolicitor(newSolicitor)
                .courtsToTransfer(courts)
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id("ORG3")
                .name("Organisation 3")
                .email("org3@test.com")
                .pbaNumber("PBA111")
                .build();

            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(oldDesignatedLocalAuthorityCode)
                .caseLocalAuthorityName(oldDesignatedAuthorityName)
                .localAuthorities(wrapElements(oldDesignatedLocalAuthority, sharedLocalAuthority))
                .build();

            when(localAuthorityService.findLocalAuthority(caseData, newDesignatedLocalAuthorityOrgId))
                .thenReturn(Optional.empty());

            final Organisation actualOrganisation = underTest.transfer(caseData);

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .designated("Yes")
                .id(newDesignatedLocalAuthorityOrgId)
                .name(newDesignatedLocalAuthorityName)
                .email(newDesignatedLocalAuthorityEmail)
                .address(address)
                .colleagues(wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .mainContact("Yes")
                    .fullName(newSolicitor.getFullName())
                    .email(newSolicitor.getEmail())
                    .notificationRecipient(YES.getValue())
                    .build()))
                .build();

            assertThat(actualOrganisation).isEqualTo(newDesignatedLocalAuthorityOrganisation);
            assertThat(caseData.getLocalAuthorities()).extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority, sharedLocalAuthority);
            assertThat(caseData.getCaseLocalAuthority()).isEqualTo(newDesignatedLocalAuthorityCode);
            assertThat(caseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthorityName);
            assertThat(caseData.getCourt()).extracting("name", "code")
                .containsExactly("Family Court sitting at Court 2", "C2");
        }
    }

    @Nested
    class TransferToAnotherCourt {

        private final Court oldCourt = Court.builder()
            .code("C1")
            .name("Court 1")
            .build();

        private final Court newCourt = Court.builder()
            .code("C2")
            .name("Court 2")
            .build();

        @BeforeEach
        void init() {
            when(courtLookUpService.buildRcjHighCourt()).thenReturn(Court.builder().code(RCJ_HIGH_COURT_CODE)
                .name(RCJ_HIGH_COURT_NAME)
                .region(RCJ_HIGH_COURT_REGION)
                .build());
        }

        @Test
        void shouldBuildPastCourtsList() {
            final CaseData caseData = CaseData.builder().build();
            when(courtService.getCourt(isA(CaseData.class))).thenReturn(
                Court.builder().code("344").name("Family Court sitting at Swansea").build()
            );

            List<Element<Court>> pastCourtList = underTest.buildPastCourtsList(caseData);
            assertThat(pastCourtList).hasSize(1);
            assertThat(unwrapElements(pastCourtList).iterator().next().getCode()).isEqualTo("344");
        }

        @Test
        void shouldTransferCourtWithoutTransferLA() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .courtsToTransferWithoutTransferLA(dynamicList(Map.of(
                        newCourt.getCode(), newCourt.getName(),
                        RCJ_HIGH_COURT_CODE, RCJ_HIGH_COURT_NAME),
                    newCourt.getCode()))
                .build();
            final CaseData caseData = CaseData.builder()
                .court(oldCourt)
                .localAuthoritiesEventData(eventData)
                .build();

            when(time.now()).thenReturn(LocalDateTime.of(1997, Month.JULY, 1, 11, 00));
            when(courtLookUpService.getCourtByCode(newCourt.getCode())).thenReturn(Optional.of(newCourt));

            final Court courtTransferred = underTest.transferCourtWithoutTransferLA(caseData);

            assertThat(courtTransferred.getCode()).isEqualTo(newCourt.getCode());
            assertThat(courtTransferred.getName()).isEqualTo("Family Court sitting at " + newCourt.getName());
            assertThat(courtTransferred.getDateTransferred()).isEqualTo(
                LocalDateTime.of(1997, Month.JULY, 1, 11, 00));
            assertThat(caseData.getPastCourtList()).hasSize(1);
            assertThat(caseData.getPastCourtList().iterator().next().getValue()).isEqualTo(oldCourt);
        }
    }

    @Nested
    class SelectedLocalAuthorityEmail {

        @Test
        void shouldReturnSelectedLocalAuthoritySharedInbox() {

            final DynamicList dynamicList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"),
                "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(dynamicList)
                .build();

            when(localAuthorityEmails.getSharedInbox("LA2")).thenReturn(Optional.of("test@test.com"));

            final String actualEmail = underTest.getSelectedLocalAuthorityEmail(eventData);

            assertThat(actualEmail).isEqualTo("test@test.com");
        }

        @Test
        void shouldReturnNullWhenSelectedLocalAuthorityDoesNotHaveSharedInbox() {

            final DynamicList dynamicList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"),
                "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(dynamicList)
                .build();

            when(localAuthorityEmails.getSharedInbox("LA2")).thenReturn(Optional.empty());

            final String actualEmail = underTest.getSelectedLocalAuthorityEmail(eventData);

            assertThat(actualEmail).isNull();
        }

        @Test
        void shouldReturnNullWhenSelectedLocalAuthorityDoesNotHaveSharedInboxn() {

            final DynamicList dynamicList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"));

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(dynamicList)
                .build();

            final String actualEmail = underTest.getSelectedLocalAuthorityEmail(eventData);

            assertThat(actualEmail).isNull();

            verifyNoInteractions(localAuthorityEmails);

        }
    }

    @Nested
    class ValidateLocalAuthorityEmail {

        @Test
        void shouldReturnValidationErrorIfEmailIsNotValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityEmail("test@test.com")
                .build();

            when(emailService.validate("test@test.com")).thenReturn(Optional.of("Validation error"));

            final List<String> actualErrors = underTest.validateLocalAuthorityToShare(eventData);

            assertThat(actualErrors).containsExactly("Validation error");
        }

        @Test
        void shouldReturnNoValidationErrorIfEmailIsValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityEmail("test@test.com")
                .build();

            when(emailService.validate("test@test.com")).thenReturn(Optional.empty());

            final List<String> actualErrors = underTest.validateLocalAuthorityToShare(eventData);

            assertThat(actualErrors).isEmpty();
        }

    }

    @Nested
    class ValidateLocalAuthorityToTransferEmails {

        private final String localAuthorityEmail = "test1@test.com";
        private final String solicitorEmail = "test2@test.com";
        private final String localAuthorityExpectedError =
            "Enter local authority's group email address in the correct format, for example name@example.com";
        private final String solicitorExpectedError =
            "Enter local authority solicitor's email address in the correct format, for example name@example.com";

        @Test
        void shouldReturnValidationErrorIfLocalAuthorityEmailIsNotValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder().email(localAuthorityEmail).build())
                .build();

            when(emailService.validate(anyString(), anyString())).thenReturn(Optional.of(localAuthorityExpectedError));

            final List<String> actualErrors = underTest.validateLocalAuthorityToTransfer(eventData);

            assertThat(actualErrors).containsExactly(localAuthorityExpectedError);

            verify(emailService).validate(localAuthorityEmail, localAuthorityExpectedError);
        }

        @Test
        void shouldReturnValidationErrorIfLocalAuthoritySolicitorEmailIsNotValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransferSolicitor(Colleague.builder().email(solicitorEmail).build())
                .build();

            when(emailService.validate(anyString(), anyString())).thenReturn(Optional.of(solicitorExpectedError));

            final List<String> actualErrors = underTest.validateLocalAuthorityToTransfer(eventData);

            assertThat(actualErrors).containsExactly(solicitorExpectedError);

            verify(emailService).validate(solicitorEmail, solicitorExpectedError);
        }

        @Test
        void shouldReturnValidateErrorIfTransferCourtWithoutTransferLAIsNotValid() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .courtsToTransferWithoutTransferLA(DynamicList.builder()
                    .value(DynamicListElement.builder().code("").build())
                    .build())
                .build();
            final List<String> actualErrors = underTest.validateTransferCourtWithoutTransferLA(eventData);
            assertThat(actualErrors).containsExactly("Invalid court selected.");
        }

        @Test
        void shouldReturnValidateErrorIfTransferCourtWithoutTransferLAIsNotSet() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .build();
            final List<String> actualErrors = underTest.validateTransferCourtWithoutTransferLA(eventData);
            assertThat(actualErrors).containsExactly("Invalid court selected.");
        }

        @Test
        void shouldNotReturnValidateErrorIfTransferCourtWithoutTransferLAIsValid() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .courtsToTransferWithoutTransferLA(DynamicList.builder()
                    .value(DynamicListElement.builder().code("100").build())
                    .build())
                .build();
            final List<String> actualErrors = underTest.validateTransferCourtWithoutTransferLA(eventData);
            assertThat(actualErrors).isEmpty();
        }

        @Test
        void shouldReturnValidationErrorIfLocalAuthoritySolicitorEmailIsNotValidX() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder().email(localAuthorityEmail).build())
                .localAuthorityToTransferSolicitor(Colleague.builder().email(solicitorEmail).build())
                .build();

            when(emailService.validate(eq(localAuthorityEmail), anyString()))
                .thenReturn(Optional.of(localAuthorityExpectedError));

            when(emailService.validate(eq(solicitorEmail), anyString()))
                .thenReturn(Optional.of(solicitorExpectedError));

            final List<String> actualErrors = underTest.validateLocalAuthorityToTransfer(eventData);

            assertThat(actualErrors).containsExactly(localAuthorityExpectedError, solicitorExpectedError);

            verify(emailService).validate(localAuthorityEmail, localAuthorityExpectedError);
            verify(emailService).validate(solicitorEmail, solicitorExpectedError);
        }

        @Test
        void shouldReturnNoValidationErrorIfEmailsAreValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityToTransfer(LocalAuthority.builder().email(localAuthorityEmail).build())
                .localAuthorityToTransferSolicitor(Colleague.builder().email(solicitorEmail).build())
                .build();

            when(emailService.validate(anyString(), anyString())).thenReturn(Optional.empty());

            final List<String> actualErrors = underTest.validateLocalAuthorityToTransfer(eventData);

            assertThat(actualErrors).isEmpty();

            verify(emailService).validate(localAuthorityEmail, localAuthorityExpectedError);
            verify(emailService).validate(solicitorEmail, solicitorExpectedError);
        }

    }

    @Nested
    class OrganisationRemovalRequest {

        @Test
        void shouldReturnChangeOrganisationRequest() {

            final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy("ORG1", "ORG name", LASHARED);

            final CaseData caseData = CaseData.builder()
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final ChangeOrganisationRequest actualChangeRequest = underTest.getOrgRemovalRequest(caseData);

            final ChangeOrganisationRequest expectedChangeOrganisationRequest = ChangeOrganisationRequest.builder()
                .approvalStatus(APPROVED)
                .requestTimestamp(time.now())
                .caseRoleId(caseRoleDynamicList(secondaryOrganisationPolicy.getOrgPolicyCaseAssignedRole()))
                .organisationToRemove(secondaryOrganisationPolicy.getOrganisation())
                .build();

            assertThat(actualChangeRequest).isEqualTo(expectedChangeOrganisationRequest);
        }

        @Test
        void shouldThrowExceptionWhenNoSharedOrganisationPolicy() {

            final CaseData caseData = CaseData.builder()
                .id(10L)
                .build();

            assertThatThrownBy(() -> underTest.getOrgRemovalRequest(caseData))
                .isInstanceOf(OrganisationPolicyNotFound.class);
        }
    }

    @Nested
    class ChangeEvent {

        final OrganisationPolicy designatedOrganisationPolicy1 = organisationPolicy("ORG1", "ORG1 name", LASOLICITOR);
        final OrganisationPolicy designatedOrganisationPolicy2 = organisationPolicy("ORG2", "ORG2 name", LASOLICITOR);
        final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy("ORG3", "ORG3 name", LASHARED);

        @Test
        void shouldReturnCaseTransferredEventWhenDesignatedOrgChanged() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .localAuthorityPolicy(designatedOrganisationPolicy2)
                .build();

            final List<Object> actualChangeEvents = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvents).containsExactly(
                new CaseTransferred(caseData, caseDataBefore),
                new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore)
            );
        }

        @Test
        void shouldReturnSecondaryLocalAuthorityAddedEvent() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final List<Object> actualChangeEvents = underTest.getChangeEvent(caseData, caseDataBefore);

            final Object expectedChangeEvent = new SecondaryLocalAuthorityAdded(caseData);

            assertThat(actualChangeEvents).containsExactly(expectedChangeEvent);
        }

        @Test
        void shouldReturnSecondaryLocalAuthorityRemovedEvent() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .sharedLocalAuthorityPolicy(null)
                .build();

            final List<Object> actualChangeEvent = underTest.getChangeEvent(caseData, caseDataBefore);

            final Object expectedChangeEvent = new SecondaryLocalAuthorityRemoved(caseData, caseDataBefore);

            assertThat(actualChangeEvent).containsExactly(expectedChangeEvent);
        }

        @Test
        void shouldReturnEmptyObjectWhenSecondaryLocalAuthorityNotRemoved() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .build();

            final List<Object> actualChangeEvents = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvents).isEmpty();
        }

        @Test
        void shouldReturnEmptyObjectWhenDesignatedLocalAuthorityHasNotChangedAndNorSecondaryLocalAuthority() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .build();

            final List<Object> actualChangeEvents = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvents).isEmpty();
        }

        @Test
        void shouldReturnEmptyObjectWhenDesignatedLocalAuthorityNorSecondaryLocalAuthorityChanged() {

            final CaseData caseDataBefore = CaseData.builder()
                .localAuthorityPolicy(designatedOrganisationPolicy1)
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .build();

            final List<Object> actualChangeEvents = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvents).isEmpty();
        }

    }

    @Nested
    class SharedLocalAuthorityPolicy {

        @Test
        void shouldReturnOrganisationPolicy() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"),
                "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            when(localAuthorityIds.getLocalAuthorityId("LA2")).thenReturn("ORG2");

            final OrganisationPolicy actualOrganisationPolicy = underTest.getSharedLocalAuthorityPolicy(caseData);

            assertThat(actualOrganisationPolicy)
                .isEqualTo(organisationPolicy("ORG2", "Local authority 2", LASHARED));
        }

        @Test
        void shouldThrowsExceptionWhenLocalAuthorityNotSelected() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"));

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            when(localAuthorityIds.getLocalAuthorityId("LA2")).thenReturn("ORG2");

            assertThatThrownBy(() -> underTest.getSharedLocalAuthorityPolicy(caseData));
        }
    }

    @Nested
    class RemoveSharedLocalAuthority {

        @Test
        void shouldRemoveSecondaryLocalAuthority() {

            final OrganisationPolicy sharedPolicy = organisationPolicy("ORG2", "Local authority 2", LASHARED);

            final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
                .id("ORG1")
                .name("Organisation 1")
                .designated("Yes")
                .build();

            final LocalAuthority sharedLocalAuthority = LocalAuthority.builder()
                .id("ORG2")
                .name("Organisation 2")
                .designated("No")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(designatedLocalAuthority, sharedLocalAuthority))
                .sharedLocalAuthorityPolicy(sharedPolicy)
                .build();

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.removeSharedLocalAuthority(caseData);

            assertThat(actualLocalAuthorities)
                .extracting(Element::getValue)
                .containsExactly(designatedLocalAuthority);
        }

        @Test
        void shouldDoNothingWhenNoSecondaryLocalAuthority() {

            final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
                .id("ORG1")
                .name("Organisation 1")
                .designated("Yes")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.removeSharedLocalAuthority(caseData);

            assertThat(actualLocalAuthorities)
                .extracting(Element::getValue)
                .containsExactly(designatedLocalAuthority);
        }

        @Test
        void shouldDoNothingWhenSecondaryOrgPolicyDoesNotHaveOrgId() {

            final OrganisationPolicy sharedPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder().build())
                .build();

            final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
                .id("ORG1")
                .name("Organisation 1")
                .designated("Yes")
                .build();

            final CaseData caseData = CaseData.builder()
                .sharedLocalAuthorityPolicy(sharedPolicy)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.removeSharedLocalAuthority(caseData);

            assertThat(actualLocalAuthorities)
                .extracting(Element::getValue)
                .containsExactly(designatedLocalAuthority);
        }
    }

    @Nested
    class AddSharedLocalAuthority {

        final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id("ORG1")
            .name("Organisation 1")
            .designated("Yes")
            .build();

        @Test
        void shouldAddSecondaryLocalAuthority() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"), "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .localAuthorityEmail("la2@test.com")
                .build();

            final LocalAuthority expectedSharedLocalAuthority = LocalAuthority.builder()
                .id("ORG2")
                .name("Organisation 2")
                .email("la2@test.com")
                .designated("No")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            uk.gov.hmcts.reform.rd.model.Organisation organisation = uk.gov.hmcts.reform.rd.model.Organisation.builder()
                .organisationIdentifier("ORG2")
                .name("Local authority 2")
                .build();

            when(localAuthorityIds.getLocalAuthorityId("LA2")).thenReturn("ORG2");
            when(organisationService.getOrganisation("ORG2")).thenReturn(organisation);
            when(localAuthorityService.getLocalAuthority(organisation)).thenReturn(expectedSharedLocalAuthority);

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.addSharedLocalAuthority(caseData);

            assertThat(actualLocalAuthorities)
                .extracting(Element::getValue)
                .containsExactly(designatedLocalAuthority, expectedSharedLocalAuthority);
        }

        @Test
        void shouldThrowsExceptionWhenOrganisationNotFound() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"), "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .localAuthorityEmail("la2@test.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            when(localAuthorityIds.getLocalAuthorityId("LA2")).thenReturn("ORG2");
            when(organisationService.getOrganisation("ORG2")).thenThrow(new OrganisationNotFound("ORG2"));

            assertThatThrownBy(() -> underTest.addSharedLocalAuthority(caseData));

            verifyNoInteractions(localAuthorityService);
            verify(localAuthorityIds).getLocalAuthorityId("LA2");
            verify(organisationService).getOrganisation("ORG2");
        }

        @Test
        void shouldThrowsExceptionWhenLocalAuthorityToOrganisationMappingNotPresent() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"), "LA2");

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .localAuthorityEmail("la2@test.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            when(localAuthorityIds.getLocalAuthorityId("LA2")).thenThrow(new UnknownLocalAuthorityException("LA2"));

            assertThatThrownBy(() -> underTest.addSharedLocalAuthority(caseData));

            verifyNoInteractions(localAuthorityService, organisationService);
            verify(localAuthorityIds).getLocalAuthorityId("LA2");
        }

        @Test
        void shouldThrowsExceptionWhenLocalAuthorityNotSelected() {

            final DynamicList localAuthoritiesList = dynamicList(Map.of(
                "LA1", "Local authority 1",
                "LA2", "Local authority 2"));

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToShare(localAuthoritiesList)
                .localAuthorityEmail("la2@test.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .build();

            assertThatThrownBy(() -> underTest.addSharedLocalAuthority(caseData));

            verifyNoInteractions(localAuthorityIds, localAuthorityService, organisationService);
        }
    }

    @Nested
    class SharedLocalAuthorityName {

        @Test
        void shouldGetSecondaryLocalAuthorityName() {

            final OrganisationPolicy sharedPolicy = organisationPolicy("ORG1", "Organisation 1", LASHARED);

            final CaseData caseData = CaseData.builder()
                .sharedLocalAuthorityPolicy(sharedPolicy)
                .build();

            final String actualLocalAuthorityName = underTest.getSharedLocalAuthorityName(caseData);

            assertThat(actualLocalAuthorityName).isEqualTo("Organisation 1");
        }

        @Test
        void shouldReturnNullWhenSecondaryLocalAuthorityNotPresent() {

            final CaseData caseData = CaseData.builder().build();

            final String actualLocalAuthorityName = underTest.getSharedLocalAuthorityName(caseData);

            assertThat(actualLocalAuthorityName).isNull();
        }

    }

    @Nested
    class CourtsToTransfer {

        private final Court court1 = Court.builder().code("C1").name("Court 1").region(LONDON.getName()).build();
        private final Court court2 = Court.builder().code("C2").name("Court 2").region(MIDLANDS.getName()).build();
        private final Court court3 = Court.builder().code("C3").name("Court 3").region(LONDON.getName()).build();
        private final Court court4 = Court.builder().code("C4").name("Court 4").region(LONDON.getName()).build();
        private final Court court5 = Court.builder().code("C5").name("Court 5").region(LONDON.getName()).build();

        private final List<Pair<String, String>> expectedCourts = List.of(
            Pair.of("C1", "Court 1"),
            Pair.of("C2", "Court 2"),
            Pair.of("C3", "Court 3"),
            Pair.of("C5", "Court 5"));

        private final List<Pair<String, String>> expectedCourtsGroupedByRegion = List.of(
            Pair.of("", "--- " + LONDON.getName() + " ---"),
            Pair.of("C5", "Court 5"),
            Pair.of("", "--- " + MIDLANDS.getName() + " ---"),
            Pair.of("C2", "Court 2"));

        private final String designatedLACode = "LA1";
        private final String secondaryLACode = "LA2";
        private final String otherLACode = "LA3";

        private final OrganisationPolicy sharedPolicy = organisationPolicy("ORG2", "Organisation 2", LASHARED);

        private final DynamicList localAuthorities = dynamicList(Map.of(
            "LA3", "Local authority 3",
            "LA4", "Local authority 4"),
            otherLACode);

        @Test
        void shouldReturnListOfSortedCourtsFromDesignatedAndSharedLocalAuthorities() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .transferToSharedLocalAuthority(YES)
                .build();

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority(designatedLACode)
                .localAuthoritiesEventData(eventData)
                .sharedLocalAuthorityPolicy(sharedPolicy)
                .build();

            final DynamicList expectedCourtList = DynamicList.builder().build();

            when(courtService.getCourt(caseData)).thenReturn(court4);
            when(courtLookup.getCourts(designatedLACode)).thenReturn(List.of(court2, court5, court1));
            when(courtLookup.getCourts(secondaryLACode)).thenReturn(List.of(court3, court1, court4));
            when(dynamicListService.asDynamicList(expectedCourts)).thenReturn(expectedCourtList);
            when(localAuthorityIds.getLocalAuthorityCode(sharedPolicy.getOrganisation().getOrganisationID()))
                .thenReturn(Optional.of(secondaryLACode));

            final DynamicList actualLocalAuthorities = underTest.getCourtsToTransfer(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedCourtList);

            verify(dynamicListService).asDynamicList(expectedCourts);
        }

        @Test
        void shouldReturnListOfSortedCourtsFromDesignatedAndSelectedLocalAuthoritiesWhenCaseIsShared() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .transferToSharedLocalAuthority(NO)
                .localAuthoritiesToTransferWithoutShared(localAuthorities)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(designatedLACode)
                .sharedLocalAuthorityPolicy(sharedPolicy)
                .build();

            final DynamicList expectedCourtList = DynamicList.builder().build();

            when(courtService.getCourt(caseData)).thenReturn(court4);
            when(courtLookup.getCourts(designatedLACode)).thenReturn(List.of(court2, court5, court1));
            when(courtLookup.getCourts(otherLACode)).thenReturn(List.of(court3, court1, court4));
            when(dynamicListService.asDynamicList(expectedCourts)).thenReturn(expectedCourtList);

            final DynamicList actualLocalAuthorities = underTest.getCourtsToTransfer(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedCourtList);

            verify(dynamicListService).asDynamicList(expectedCourts);
        }

        @Test
        void shouldReturnListOfSortedCourtsFromDesignatedAndSelectedLocalAuthoritiesWhenCaseIsNotShared() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthoritiesToTransfer(localAuthorities)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .caseLocalAuthority(designatedLACode)
                .build();

            final DynamicList expectedCourtList = DynamicList.builder().build();

            when(courtService.getCourt(caseData)).thenReturn(court4);
            when(courtLookup.getCourts(designatedLACode)).thenReturn(List.of(court2, court5, court1));
            when(courtLookup.getCourts(otherLACode)).thenReturn(List.of(court3, court1, court4));
            when(dynamicListService.asDynamicList(expectedCourts)).thenReturn(expectedCourtList);

            final DynamicList actualLocalAuthorities = underTest.getCourtsToTransfer(caseData);

            assertThat(actualLocalAuthorities).isEqualTo(expectedCourtList);

            verify(dynamicListService).asDynamicList(expectedCourts);
        }

        @Test
        void shouldReturnFullListOfSortedCourtsWithCourtRegionGrouped() {
            final CaseData caseData = CaseData.builder().build();

            final DynamicList expectedCourtList = DynamicList.builder().build();

            when(courtLookUpService.getCourtFullListWithRcjHighCourt()).thenReturn(List.of(court2, court5, court1));
            when(courtService.getCourt(caseData)).thenReturn(court1);
            when(dynamicListService.asDynamicList(expectedCourtsGroupedByRegion)).thenReturn(expectedCourtList);

            final DynamicList actualLocalAuthorities = underTest.getCourtsToTransferWithHighCourt(caseData, true);

            assertThat(actualLocalAuthorities).isEqualTo(expectedCourtList);

            verify(dynamicListService).asDynamicList(expectedCourtsGroupedByRegion);
        }
    }

    private static DynamicList dynamicList(Map<String, String> elements) {
        return dynamicList(elements, null);
    }

    private static DynamicList dynamicList(Map<String, String> elements, String selected) {
        final List<DynamicListElement> items = elements.entrySet().stream()
            .map(element -> DynamicListElement.builder()
                .code(element.getKey())
                .label(element.getValue())
                .build())
            .collect(Collectors.toList());

        final DynamicListElement selectedItem = items.stream()
            .filter(item -> Objects.equals(selected, item.getCode()))
            .findFirst()
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder()
            .value(selectedItem)
            .listItems(items)
            .build();
    }
}
