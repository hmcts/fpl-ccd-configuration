package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.Representative;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CAFCASSSOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.LA_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@ExtendWith(SpringExtension.class)
class RepresentativeCaseRoleServiceTest {

    private RepresentativeCaseRoleService representativesChangeService = new RepresentativeCaseRoleService();

    @Test
    void shouldReturnEmptyCaseRoleUpdatesWhenNoRepresentatives() {
        assertCaseRoleUpdates(
            emptyList(),
            emptyList(),
            emptyMap());
    }

    @Test
    void shouldReturnCaseRoleUpdatesForAddedRepresentative() {
        Representative representative = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("representative@test.com")
            .build();

        assertCaseRoleUpdates(
            List.of(representative),
            emptyList(),
            Map.of(representative.getEmail(), Set.of(SOLICITOR)));
    }

    @Test
    void shouldReturnCaseRoleUpdatesForDeletedRepresentative() {
        Representative representative = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("representative@test.com")
            .build();

        assertCaseRoleUpdates(
            emptyList(),
            List.of(representative),
            Map.of(representative.getEmail(), emptySet()));
    }

    @Test
    void shouldReturnCaseRoleUpdatesWhenRepresentativeEmailChanged() {
        Representative originalRepresentative = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("representative@test.com")
            .build();

        Representative updatedRepresentative = originalRepresentative.toBuilder()
            .email("updated-representative@test.com")
            .build();

        assertCaseRoleUpdates(
            List.of(updatedRepresentative),
            List.of(originalRepresentative),
            Map.ofEntries(
                entry(originalRepresentative.getEmail(), emptySet()),
                entry(updatedRepresentative.getEmail(), Set.of(SOLICITOR))
            ));
    }

    @Test
    void shouldReturnEmptyCaseRoleUpdatesWhenRepresentativeEmailChangesButRepresentSameEmail() {
        Representative originalRepresentative = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("representative@test.com")
            .build();

        Representative updatedRepresentative = originalRepresentative.toBuilder()
            .email("Representative@Test.com")
            .build();

        assertCaseRoleUpdates(
            List.of(updatedRepresentative),
            List.of(originalRepresentative),
            emptyMap());
    }

    @Test
    void shouldReturnCaseRoleUpdatesWhenRepresentativeRoleAndEmailChanged() {
        Representative originalRepresentative = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("representative@test.com")
            .build();

        Representative updatedRepresentative = originalRepresentative.toBuilder()
            .role(LA_LEGAL_REPRESENTATIVE)
            .email("updated-representative@test.com")
            .build();

        assertCaseRoleUpdates(
            List.of(updatedRepresentative),
            List.of(originalRepresentative),
            Map.ofEntries(
                entry(originalRepresentative.getEmail(), emptySet()),
                entry(updatedRepresentative.getEmail(), Set.of(LASOLICITOR))
            ));
    }

    @Test
    void shouldCombineRolesWhenSameRepresentativeHasMultipleRoles() {
        String email = "representative@test.com";

        Representative newRepresentative1 = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email(email)
            .build();

        Representative newRepresentative2 = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_2)
            .email(email)
            .build();

        Representative newRepresentative3 = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(LA_LEGAL_REPRESENTATIVE)
            .email(email)
            .build();

        assertCaseRoleUpdates(
            List.of(newRepresentative1, newRepresentative2, newRepresentative3),
            emptyList(),
            Map.of(email, Set.of(SOLICITOR, LASOLICITOR)));
    }

    @Test
    void shouldReturnCaseRoleUpdatesForMultipleRepresentatives() {

        Representative originalRepresentativeA = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("A@test.com")
            .build();

        Representative originalRepresentativeB = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_2)
            .email("B@test.com")
            .build();

        Representative originalRepresentativeC = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(LA_LEGAL_REPRESENTATIVE)
            .email("C@test.com")
            .build();

        Representative updatedRepresentativeB = originalRepresentativeB.toBuilder()
            .role(LA_LEGAL_REPRESENTATIVE)
            .build();

        Representative updatedRepresentativeC = originalRepresentativeC.toBuilder()
            .build();

        Representative newRepresentativeD = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_RESPONDENT_1)
            .email("D@test.com")
            .build();

        assertCaseRoleUpdates(
            List.of(updatedRepresentativeB, updatedRepresentativeC, newRepresentativeD),
            List.of(originalRepresentativeA, originalRepresentativeB, originalRepresentativeC),
            Map.ofEntries(
                entry(originalRepresentativeA.getEmail(), emptySet()),
                entry(originalRepresentativeB.getEmail(), Set.of(LASOLICITOR)),
                entry(newRepresentativeD.getEmail(), Set.of(SOLICITOR))
            ));
    }

    @Nested
    @DisplayName("Role changed")
    class RoleChange {
        @Test
        void shouldReturnCaseRoleUpdatesWhenRepresentativeRolesChangedToLocalAuthorityLegalRepresentative() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .role(LA_LEGAL_REPRESENTATIVE)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(originalRepresentative.getEmail(), Set.of(LASOLICITOR)));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenRepresentativeRoleChangedToCafcassGuardian() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .role(CAFCASS_GUARDIAN)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.ofEntries(
                    entry(originalRepresentative.getEmail(), emptySet())
                ));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenRepresentativeRoleChangedToCafcassSolicitor() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .role(CAFCASS_SOLICITOR)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(originalRepresentative.getEmail(), Set.of(CAFCASSSOLICITOR)));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenRepresentedPartyChanged() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .role(REPRESENTING_PERSON_1)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                emptyMap());
        }
    }

    @Nested
    @DisplayName("Serving preferences changed")
    class ServingPreferencesChange {

        @Test
        void shouldReturnCaseRoleUpdatesWhenServingPreferenceChangedFromDigitalServiceToEmail() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .servingPreferences(EMAIL)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(originalRepresentative.getEmail(), emptySet()));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenServingPreferenceChangedFromDigitalServiceToPost() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .servingPreferences(POST)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(originalRepresentative.getEmail(), emptySet()));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenServingPreferenceChangedFromEmailToDigitalService() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .email("representative@test.com")
                .servingPreferences(EMAIL)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(originalRepresentative.getEmail(), Set.of(SOLICITOR)));
        }

        @Test
        void shouldReturnCaseRoleUpdatesWhenServingPreferenceChangedFromPostToDigitalService() {
            Representative originalRepresentative = Representative.builder()
                .role(REPRESENTING_RESPONDENT_1)
                .servingPreferences(POST)
                .build();

            Representative updatedRepresentative = originalRepresentative.toBuilder()
                .email("representative@test.com")
                .servingPreferences(DIGITAL_SERVICE)
                .build();

            assertCaseRoleUpdates(
                List.of(updatedRepresentative),
                List.of(originalRepresentative),
                Map.of(updatedRepresentative.getEmail(), Set.of(SOLICITOR)));
        }
    }

    private void assertCaseRoleUpdates(List<Representative> updatedRepresentatives,
                                       List<Representative> originalRepresentatives,
                                       Map<String, Set<CaseRole>> expectedUpdates) {
        Map<String, Set<CaseRole>> actualUpdates = representativesChangeService
            .calculateCaseRoleUpdates(updatedRepresentatives, originalRepresentatives);

        assertThat(actualUpdates).isEqualTo(expectedUpdates);
    }


}
