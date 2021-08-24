package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class CaseSummaryPeopleInCaseGeneratorTest {

    private static final String SOLICITOR_NAME = "Solicitor Name";
    private static final String SOLICITOR_EMAIL = "solicitor@email.com";
    private static final String RESPONDENT_LAST_NAME = "Respondent last name";
    private static final String LEGAL_REPRESENTATIVE = "Legal Representative";
    private static final RepresentativeRole A_NON_CAFCASS_GUARDIAN_ROLE = REPRESENTING_OTHER_PERSON_1;
    private static final String CAFCASS_GUARDIAN_FULL_NAME = "Cafcass guardian full name";
    private static final String ANOTHER_CAFCASS_GUARDIAN_FULL_NAME = "Another Cafcass guardian full name";

    private final CaseSummaryPeopleInCaseGenerator underTest = new CaseSummaryPeopleInCaseGenerator();

    @Test
    void testNoFields() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testEmptyChildren() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .children1(emptyList())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testChildren() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .children1(List.of(
                element(mock(Child.class)),
                element(mock(Child.class))
            )).build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryNumberOfChildren(2)
            .build());
    }

    @Nested
    class MainContact {

        final Solicitor legacySolicitor = Solicitor.builder()
            .name(SOLICITOR_NAME)
            .email(SOLICITOR_EMAIL)
            .build();

        @Test
        void shouldPopulateMainContactFromDesignatedLocalAuthority() {
            final Colleague colleague1 = Colleague.builder()
                .fullName("John Smith")
                .email("john.smith@test.com")
                .mainContact("Yes")
                .build();

            final Colleague colleague2 = Colleague.builder()
                .fullName("Alex Williams")
                .mainContact("No")
                .build();

            final Colleague colleague3 = Colleague.builder()
                .fullName("Emma White")
                .mainContact("Yes")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .name("LA")
                .designated("Yes")
                .colleagues(wrapElements(colleague1, colleague2))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .name("LA2")
                .designated("No")
                .colleagues(wrapElements(colleague3))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .solicitor(legacySolicitor)
                .build();

            final SyntheticCaseSummary actual = underTest.generate(caseData);

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .caseSummaryLASolicitorName("John Smith")
                .caseSummaryLASolicitorEmail("john.smith@test.com")
                .build());
        }

        @Test
        void shouldNotPopulateMainContactWhenDesignatedLocalAuthorityDoesNotHaveColleagues() {
            final Colleague colleague = Colleague.builder()
                .mainContact("Yes")
                .fullName("John Smith")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .designated("Yes")
                .name("LA")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .designated("No")
                .name("LA")
                .colleagues(wrapElements(colleague))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .solicitor(legacySolicitor)
                .build();

            final SyntheticCaseSummary actual = underTest.generate(caseData);

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .build());
        }

        @Test
        void shouldNotPopulateMainContactWhenDesignatedLocalAuthorityDoesNotHaveOne() {
            final Colleague colleague1 = Colleague.builder()
                .fullName("John Smith")
                .email("john.smith@test.com")
                .mainContact("No")
                .build();

            final Colleague colleague2 = Colleague.builder()
                .fullName("Alex Williams")
                .mainContact("No")
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .name("LA")
                .designated("Yes")
                .colleagues(wrapElements(colleague1, colleague2))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority))
                .solicitor(legacySolicitor)
                .build();

            final SyntheticCaseSummary actual = underTest.generate(caseData);

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .build());
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldPopulateMainContactFromLegacySolicitorWhenNoLocalAuthorities(List<LocalAuthority> localAuthorities) {
            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthorities))
                .solicitor(legacySolicitor)
                .build();

            final SyntheticCaseSummary actual = underTest.generate(caseData);

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .caseSummaryLASolicitorName(SOLICITOR_NAME)
                .caseSummaryLASolicitorEmail(SOLICITOR_EMAIL)
                .build());
        }

        @Test
        void shouldNotPopulateMainContactWhenNoLocalAuthoritiesNorLegacySolicitor() {
            final CaseData caseData = CaseData.builder()
                .build();

            final SyntheticCaseSummary actual = underTest.generate(caseData);

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .build());
        }
    }

    @Test
    void testEmptyFirstRespondentLastName() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .respondents1(emptyList())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .build());
    }

    @Test
    void testFirstRespondentLastNameNoParty() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .respondents1(List.of(
                element(Respondent.builder().build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .build());
    }

    @Test
    void testFirstRespondentLastName() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .respondents1(List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName(RESPONDENT_LAST_NAME)
                        .build())
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryFirstRespondentLastName(RESPONDENT_LAST_NAME)
            .build());
    }

    @Test
    void testNoRespondentLegalRep() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(emptyList())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder().build());
    }

    @Test
    void testFirstRespondentLegalRep() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .role(REPRESENTING_RESPONDENT_1)
                    .fullName(LEGAL_REPRESENTATIVE)
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryFirstRespondentLegalRep(LEGAL_REPRESENTATIVE)
            .build());
    }

    @Test
    void testFirstRespondentLegalRepMissingRespondent1() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .role(REPRESENTING_RESPONDENT_2)
                    .fullName(LEGAL_REPRESENTATIVE)
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder().build());
    }

    @Test
    void testRepresentativesWithoutRole() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .fullName(LEGAL_REPRESENTATIVE)
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testNoCafcassGuardianNoRepresentatives() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .role(A_NON_CAFCASS_GUARDIAN_ROLE)
                    .fullName(LEGAL_REPRESENTATIVE)
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder().build());
    }

    @Test
    void testSingleCafcassGuardian() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .role(CAFCASS_GUARDIAN)
                    .fullName(CAFCASS_GUARDIAN_FULL_NAME)
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryCafcassGuardian(CAFCASS_GUARDIAN_FULL_NAME)
            .build());
    }

    @Test
    void testMultipleCafcassGuardians() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .representatives(List.of(
                element(Representative.builder()
                    .role(CAFCASS_GUARDIAN)
                    .fullName(CAFCASS_GUARDIAN_FULL_NAME)
                    .build()),
                element(Representative.builder()
                    .role(CAFCASS_GUARDIAN)
                    .fullName(ANOTHER_CAFCASS_GUARDIAN_FULL_NAME)
                    .build()
                )))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryCafcassGuardian(String.format("%s, %s",
                CAFCASS_GUARDIAN_FULL_NAME,
                ANOTHER_CAFCASS_GUARDIAN_FULL_NAME))
            .build());
    }

}
