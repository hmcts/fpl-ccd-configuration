package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class RespondentTest {

    @Nested
    class OrganisationSpecifiedForRespondentSolicitorTests {

        @Test
        void shouldReturnTrueWhenLegalRepresentationNotNeeded() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build();

            assertTrue(respondent.hasRequiredSolicitorOrganisationDetails());
        }

        @Test
        void shouldReturnFalseWhenLegalRepresentationNeededButSolicitorDetailsMissing() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .build();

            assertFalse(respondent.hasRequiredSolicitorOrganisationDetails());
        }

        @Test
        void shouldReturnFalseWhenLegalRepresentationNeededButOrganisationDetailsMissing() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder().firstName("Paul").build())
                .build();

            assertFalse(respondent.hasRequiredSolicitorOrganisationDetails());
        }

        @Test
        void shouldReturnTrueWhenLegalRepresentationNeededAndRegisteredOrganisationSelected() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder()
                    .firstName("Paul")
                    .organisation(Organisation.builder().organisationID("test ID").build())
                    .build())
                .build();

            assertTrue(respondent.hasRequiredSolicitorOrganisationDetails());
        }

        @Test
        void shouldReturnTrueWhenLegalRepresentationNeededAndUnregisteredOrganisationDetailsEntered() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder()
                    .firstName("Paul")
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("unregistered org").build())
                    .build())
                .build();

            assertTrue(respondent.hasRequiredSolicitorOrganisationDetails());
        }
    }

    @Nested
    class EmailEnteredWhenRequiredTests {

        @Test
        void shouldReturnTrueWhenLegalRepresentationIsNotNeeded() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build();

            assertTrue(respondent.isEmailEnteredWhenRequired());
        }

        @Test
        void shouldReturnFalseWhenLegalRepresentationNeededAndEmailNotEntered() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder().build())
                .build();

            assertFalse(respondent.isEmailEnteredWhenRequired());
        }

        @Test
        void shouldReturnTrueWhenLegalRepresentationNeededAndEmailEntered() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder().email("test email").build())
                .build();

            assertTrue(respondent.isEmailEnteredWhenRequired());
        }
    }

    @Nested
    class HasRegisteredOrganisation {

        private Respondent underTest;

        @Test
        void shouldReturnTrueWhenRegisteredOrgIdPresent() {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .organisation(Organisation.builder()
                        .organisationID("someOrgId")
                        .build())
                    .build())
                .build();

            assertThat(underTest.hasRegisteredOrganisation()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseWhenRegisteredOrgIdNotPresent(String id) {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .organisation(Organisation.builder()
                        .organisationID(id)
                        .build())
                    .build())
                .build();

            assertThat(underTest.hasRegisteredOrganisation()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenRegisteredOrganisationNotPresent() {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .organisation(null)
                    .build())
                .build();

            assertThat(underTest.hasRegisteredOrganisation()).isFalse();
        }

    }

    @Nested
    class HasUnregisteredOrganisation {

        private Respondent underTest;

        @Test
        void shouldReturnTrueWhenUnregisteredOrgNamePresent() {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered org name").build())
                    .build())
                .build();

            assertThat(underTest.hasUnregisteredOrganisation()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseWhenUnregisteredOrgNameNotPresent(String orgName) {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name(orgName).build())
                    .build())
                .build();

            assertThat(underTest.hasUnregisteredOrganisation()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenUnregisteredOrganisationNotPresent() {
            underTest = Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .unregisteredOrganisation(null)
                    .build())
                .build();

            assertThat(underTest.hasUnregisteredOrganisation()).isFalse();
        }
    }

    @Nested
    class HasLegalRepresentationTests {
        @Test
        void shouldReturnTrueWhenRepresentedByNotSet() {
            Respondent respondent = Respondent.builder()
                .legalRepresentation(YES.getValue())
                .build();

            assertTrue(respondent.hasLegalRepresentation());
        }

        @Test
        void shouldReturnFalseWhenNoLegalRepresentation() {
            Respondent respondent = Respondent.builder()
                .build();

            assertFalse(respondent.hasLegalRepresentation());
        }

        @Test
        void shouldReturnTrueWhenRepresentedBySet() {
            Respondent respondent = Respondent.builder()
                .representedBy(List.of(element(randomUUID())))
                .build();

            assertTrue(respondent.hasLegalRepresentation());
        }
    }
}
