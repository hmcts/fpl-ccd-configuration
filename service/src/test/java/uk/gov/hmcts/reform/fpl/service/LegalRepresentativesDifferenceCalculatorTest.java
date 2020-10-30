package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentativesChange;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class LegalRepresentativesDifferenceCalculatorTest {

    public static final LegalRepresentativeRole ROLE = LegalRepresentativeRole.EXTERNAL_LA_BARRISTER;
    public static final LegalRepresentativeRole ANOTHER_ROLE = LegalRepresentativeRole.EXTERNAL_LA_SOLICITOR;
    public static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .email("email1")
        .role(ROLE)
        .telephoneNumber("0454444")
        .fullName("Sandro")
        .organisation("Organisation")
        .build();
    public static final LegalRepresentative ANOTHER_LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .email("email2")
        .role(ANOTHER_ROLE)
        .telephoneNumber("343543543")
        .fullName("Jonny")
        .organisation("Organisation2")
        .build();
    public static final String ANOTHER_EMAIL = "anotherEmail";


    private final LegalRepresentativesDifferenceCalculator underTest = new LegalRepresentativesDifferenceCalculator();

    @Test
    void testEmpty() {
        LegalRepresentativesChange actual = underTest.calculate(
            emptyList(),
            emptyList()
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(emptySet())
            .removed(emptySet())
            .build());
    }

    @Test
    void testAdded() {
        LegalRepresentativesChange actual = underTest.calculate(
            emptyList(),
            List.of(LEGAL_REPRESENTATIVE)
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE))
            .removed(emptySet())
            .build());
    }

    @Test
    void testAddedUserMaintainPreExisting() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE),
            List.of(LEGAL_REPRESENTATIVE, ANOTHER_LEGAL_REPRESENTATIVE)
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(Set.of(ANOTHER_LEGAL_REPRESENTATIVE))
            .removed(emptySet())
            .build());
    }

    @Test
    void testRemoved() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE),
            emptyList()
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(emptySet())
            .removed(Set.of(LEGAL_REPRESENTATIVE))
            .build());
    }

    @Test
    void testRemoveUserMaintainPreExisting() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE, ANOTHER_LEGAL_REPRESENTATIVE),
            List.of(LEGAL_REPRESENTATIVE)
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(emptySet())
            .removed(Set.of(ANOTHER_LEGAL_REPRESENTATIVE))
            .build());
    }

    @Test
    void testChangedEmail() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE),
            List.of(LEGAL_REPRESENTATIVE.toBuilder()
                .email(ANOTHER_EMAIL).build())
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(Set.of(LEGAL_REPRESENTATIVE.toBuilder().email(ANOTHER_EMAIL).build()))
            .removed(Set.of(LEGAL_REPRESENTATIVE.toBuilder().build()))
            .build());
    }

    @Test
    void testChangedNonRelevantFields() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE),
            List.of(LEGAL_REPRESENTATIVE.toBuilder()
                .telephoneNumber("23243")
                .organisation("Organisation 2")
                .fullName("Another name")
                .role(ANOTHER_ROLE)
                .build())
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(emptySet())
            .removed(emptySet())
            .build());
    }

    @Test
    void testAddedRoleForSameUser() {
        LegalRepresentativesChange actual = underTest.calculate(
            List.of(LEGAL_REPRESENTATIVE),
            List.of(LEGAL_REPRESENTATIVE,
                LEGAL_REPRESENTATIVE.toBuilder()
                    .role(ANOTHER_ROLE).build()
            )
        );

        assertThat(actual).isEqualTo(LegalRepresentativesChange.builder()
            .added(emptySet())
            .removed(emptySet())
            .build());
    }

}
