package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

class RespondentTest {

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
    class HasAddressAdded {

        private Respondent underTest;

        @Test
        void shouldReturnTrueWhenRespondentPartyHasAddress() {
            underTest = Respondent.builder()
                .party(RespondentParty.builder().address(testAddress()).build())
                .build();

            assertTrue(underTest.hasAddress());
        }

        @Test
        void shouldReturnFalseWhenRespondentPartyIsNull() {
            underTest = Respondent.builder().build();

            assertFalse(underTest.hasAddress());
        }

        @Test
        void shouldReturnFalseWhenPostcodeIsMissing() {
            underTest = Respondent.builder()
                .party(RespondentParty.builder().address(Address.builder().addressLine1("address1").build()).build())
                .build();

            assertFalse(underTest.hasAddress());
        }

        @Test
        void shouldReturnFalseWhenAddressIsMissing() {
            underTest = Respondent.builder()
                .party(RespondentParty.builder().firstName("first").lastName("name").build())
                .build();

            assertThat(underTest.hasUnregisteredOrganisation()).isFalse();
        }

    }
}
