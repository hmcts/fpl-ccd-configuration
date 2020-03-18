package uk.gov.hmcts.reform.fpl.model.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTest {

    // Using as an example of party as party doesn't have a builder and adding one introduces complications
    private RespondentParty.RespondentPartyBuilder builder;

    @BeforeEach
    void setUp() {
        builder = RespondentParty.builder();
    }

    @Test
    void shouldReturnAnEmptyStringWhenFirstNameAndLastNameAreNull() {
        final String fullName = builder.build().getFullName();

        assertThat(fullName).isBlank();
    }

    @Test
    void shouldReturnAnFirstAndLastNameWhenBothArePopulated() {
        builder.firstName("Bob").lastName("Ross");

        final String fullName = builder.build().getFullName();

        assertThat(fullName).isEqualTo("Bob Ross");
    }

    @Test
    void shouldReturnLastNameOnlyWhenFirstNameIsNull() {
        builder.lastName("Ross");

        final String fullName = builder.build().getFullName();

        assertThat(fullName).isEqualTo("Ross");
    }

    @Test
    void shouldReturnLastNameOnlyWhenFirstNameIsBlank() {
        builder.firstName("").lastName("Ross");

        final String fullName = builder.build().getFullName();

        assertThat(fullName).isEqualTo("Ross");
    }

    @Test
    void shouldReturnFirstNameOnlyWhenLastNameIsNull() {
        builder.firstName("Bob");

        final String fullName = builder.build().getFullName();

        assertThat(fullName).isEqualTo("Bob");
    }


    @Test
    void shouldReturnFirstNameOnlyWhenLastNameIsBlank() {
        builder.firstName("Bob").lastName("");

        final String fullName = builder.build().getFullName();

        assertThat(fullName).isEqualTo("Bob");
    }
}
