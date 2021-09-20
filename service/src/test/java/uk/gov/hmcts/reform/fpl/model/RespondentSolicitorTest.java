package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RespondentSolicitorTest {
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Smith";

    private final UnregisteredOrganisation unregisteredOrganisation = mock(UnregisteredOrganisation.class);
    private final Organisation organisation = mock(Organisation.class);

    @Test
    void shouldReturnFullNameWhenFullNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        String expectedName = String.format("%s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME);

        assertThat(respondentSolicitor.getFullName()).isEqualTo(expectedName);
    }

    @Test
    void shouldReturnFullNameWhenOnlyFirstNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .build();

        assertThat(respondentSolicitor.getFullName()).isEqualTo(SOLICITOR_FIRST_NAME);
    }

    @Test
    void shouldReturnFullNameWhenOnlyLastNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        assertThat(respondentSolicitor.getFullName()).isEqualTo(SOLICITOR_LAST_NAME);
    }

    @Test
    void hasFullNameOnlyFirstName() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        assertThat(respondentSolicitor.hasFullName()).isFalse();
    }

    @Test
    void hasFullNameOnlyLastName() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .build();

        assertThat(respondentSolicitor.hasFullName()).isFalse();
    }

    @Test
    void hasFullName() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        assertThat(respondentSolicitor.hasFullName()).isTrue();
    }

    @Test
    void hasOrganisationDetailsUnregistered() {
        when(unregisteredOrganisation.getName()).thenReturn("some string");

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .unregisteredOrganisation(unregisteredOrganisation)
            .build();

        assertThat(respondentSolicitor.hasOrganisationDetails()).isTrue();
    }

    @Test
    void hasOrganisationDetailsRegistered() {
        when(organisation.getOrganisationID()).thenReturn("some string");

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(organisation)
            .build();

        assertThat(respondentSolicitor.hasOrganisationDetails()).isTrue();
    }

    @Test
    void hasOrganisationDetailsNoOrganisations() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder().build();

        assertThat(respondentSolicitor.hasOrganisationDetails()).isFalse();
    }

    @Test
    void hasOrganisationDetailsNoUnregisteredOrganisationName() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .unregisteredOrganisation(unregisteredOrganisation)
            .build();

        assertThat(respondentSolicitor.hasOrganisationDetails()).isFalse();
    }

    @Test
    void hasOrganisationDetailsNoRegisteredOrganisationName() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .organisation(organisation)
            .build();

        assertThat(respondentSolicitor.hasOrganisationDetails()).isFalse();
    }
}
