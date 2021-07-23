package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationPolicyNotFound;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(MockitoExtension.class)
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
    private LocalAuthorityNameLookupConfiguration localAuthorities;

    @Mock
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmails;

    @Mock
    private LocalAuthorityIdLookupConfiguration localAuthorityId;

    @Mock
    private ApplicantLocalAuthorityService localAuthorityService;

    @InjectMocks
    private ManageLocalAuthoritiesService underTest;

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
                "Case access has already been given to local authority. Remove their access to continue");
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
        void shouldReturnErrorWhenUserTriesToTransferCase() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final List<String> actualErrors = underTest.validateAction(caseData);

            assertThat(actualErrors).containsExactly("Transfer of case is not supported yet");
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

            when(localAuthorityEmails.getLocalAuthority("LA2")).thenReturn(Optional.of("test@test.com"));

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

            when(localAuthorityEmails.getLocalAuthority("LA2")).thenReturn(Optional.empty());

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

            final List<String> actualErrors = underTest.validateLocalAuthorityEmail(eventData);

            assertThat(actualErrors).containsExactly("Validation error");
        }

        @Test
        void shouldReturnNoValidationErrorIfEmailIsValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityEmail("test@test.com")
                .build();

            when(emailService.validate("test@test.com")).thenReturn(Optional.empty());

            final List<String> actualErrors = underTest.validateLocalAuthorityEmail(eventData);

            assertThat(actualErrors).isEmpty();
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

        @Test
        void shouldReturnSecondaryLocalAuthorityAddedEvent() {

            final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy("ORG1", "ORG name", LASHARED);

            final CaseData caseDataBefore = CaseData.builder()
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final Optional<Object> actualChangeEvent = underTest.getChangeEvent(caseData, caseDataBefore);

            final Object expectedChangeEvent = new SecondaryLocalAuthorityAdded(caseData);

            assertThat(actualChangeEvent).contains(expectedChangeEvent);
        }

        @Test
        void shouldReturnSecondaryLocalAuthorityRemovedEvent() {

            final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy("ORG1", "ORG name", LASHARED);

            final CaseData caseDataBefore = CaseData.builder()
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .sharedLocalAuthorityPolicy(null)
                .build();

            final Optional<Object> actualChangeEvent = underTest.getChangeEvent(caseData, caseDataBefore);

            final Object expectedChangeEvent = new SecondaryLocalAuthorityRemoved(caseData, caseDataBefore);

            assertThat(actualChangeEvent).contains(expectedChangeEvent);
        }

        @Test
        void shouldReturnEmptyObjectWhenSecondaryLocalAuthorityNotRemoved() {

            final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy("ORG1", "ORG name", LASHARED);

            final CaseData caseDataBefore = CaseData.builder()
                .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .build();

            final Optional<Object> actualChangeEvent = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvent).isEmpty();
        }

        @Test
        void shouldReturnEmptyObjectWhenSecondaryLocalAuthorityNotAdded() {

            final CaseData caseDataBefore = CaseData.builder()
                .build();

            final CaseData caseData = caseDataBefore.toBuilder()
                .build();

            final Optional<Object> actualChangeEvent = underTest.getChangeEvent(caseData, caseDataBefore);

            assertThat(actualChangeEvent).isEmpty();
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

            when(localAuthorityId.getLocalAuthorityId("LA2")).thenReturn("ORG2");

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

            when(localAuthorityId.getLocalAuthorityId("LA2")).thenReturn("ORG2");

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

            when(localAuthorityId.getLocalAuthorityId("LA2")).thenReturn("ORG2");
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

            when(localAuthorityId.getLocalAuthorityId("LA2")).thenReturn("ORG2");
            when(organisationService.getOrganisation("ORG2")).thenThrow(new OrganisationNotFound("ORG2"));

            assertThatThrownBy(() -> underTest.addSharedLocalAuthority(caseData));

            verifyNoInteractions(localAuthorityService);
            verify(localAuthorityId).getLocalAuthorityId("LA2");
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

            when(localAuthorityId.getLocalAuthorityId("LA2")).thenThrow(new UnknownLocalAuthorityException("LA2"));

            assertThatThrownBy(() -> underTest.addSharedLocalAuthority(caseData));

            verifyNoInteractions(localAuthorityService, organisationService);
            verify(localAuthorityId).getLocalAuthorityId("LA2");
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

            verifyNoInteractions(localAuthorityId, localAuthorityService, organisationService);
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
