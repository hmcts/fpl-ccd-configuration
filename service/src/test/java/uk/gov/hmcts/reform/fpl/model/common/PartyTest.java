package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTest {

    private Party party;

    @Test
    void shouldReturnAnEmptyStringWhenFirstNameAndLastNameAreNull() {
        party = pseudoPartyBuilder(null, null);

        final String fullName = party.getFullName();

        assertThat(fullName).isBlank();
    }

    @Test
    void shouldReturnAnFirstAndLastNameWhenBothArePopulated() {
        party = pseudoPartyBuilder("Bob", "Ross");

        final String fullName = party.getFullName();

        assertThat(fullName).isEqualTo("Bob Ross");
    }

    @Test
    void shouldReturnLastNameOnlyWhenFirstNameIsNull() {
        party = pseudoPartyBuilder(null, "Ross");

        final String fullName = party.getFullName();

        assertThat(fullName).isEqualTo("Ross");
    }

    @Test
    void shouldReturnLastNameOnlyWhenFirstNameIsBlank() {
        party = pseudoPartyBuilder("", "Ross");

        final String fullName = party.getFullName();

        assertThat(fullName).isEqualTo("Ross");
    }

    @Test
    void shouldReturnFirstNameOnlyWhenLastNameIsNull() {
        party = pseudoPartyBuilder("Bob", null);

        final String fullName = party.getFullName();

        assertThat(fullName).isEqualTo("Bob");
    }


    @Test
    void shouldReturnFirstNameOnlyWhenLastNameIsBlank() {
        party = pseudoPartyBuilder("Bob", "");

        final String fullName = party.getFullName();

        assertThat(fullName).isEqualTo("Bob");
    }

    private Party pseudoPartyBuilder(String firstName, String lastName) {
        return new Party("", null, firstName, lastName, "", null, null, null, null);
    }
}
