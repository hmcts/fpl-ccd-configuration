package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

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
}
