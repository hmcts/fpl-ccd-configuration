package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class LocalAuthorityDetailsCheckerTest {

    private LocalAuthorityDetailsChecker underTest = new LocalAuthorityDetailsChecker();

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorMessageWhenLocalAuthoritiesDetailsNotAdded(List<Element<LocalAuthority>> localAuthorities) {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities)
            .build();

        assertThat(underTest.validate(caseData))
            .containsExactly("Add applicant's details|Ychwanegu manylion yr ymgeisydd");
    }

    @Test
    void shouldReturnErrorsWhenLocalAuthoritiesDetailsNotPresent() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder().build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Enter local authority's name",
            "Enter local authority's pba number",
            "Enter local authority's address",
            "Enter local authority's phone number",
            "Add a colleague"
        );
    }

    @Test
    void shouldReturnAddressErrors() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority().toBuilder()
                .address(Address.builder().build())
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Enter local authority's postcode",
            "Enter valid local authority's address"
        );
    }

    @Test
    void shouldNotReturnErrorsWhenAllLocalAuthoritiesDetailsPresent() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()))
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenNoSolicitorAdded() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                .toBuilder()
                .colleagues(wrapElements(Colleague.builder()
                    .role(ColleagueRole.SOCIAL_WORKER)
                    .fullName("Alex Brown")
                    .email("test@test.com")
                    .notificationRecipient("Yes")
                    .build()))
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenNoColleagueAdded() {
        final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                        .toBuilder()
                        .colleagues(Collections.emptyList())
                        .build()))
                .build();
        assertThat(underTest.validate(caseData))
                .containsExactly("Add a colleague");
    }

    @Test
    void shouldReturnErrorsForSingleColleague() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                .toBuilder()
                .colleagues(wrapElements(Colleague.builder()
                    .fullName("Alex Brown")
                    .build()))
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Select colleague case role",
            "Enter colleague email",
            "Select send them case update notifications"
        );
    }

    @Test
    void shouldReturnErrorsForMultipleColleagues() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                .toBuilder()
                .colleagues(wrapElements(
                    Colleague.builder()
                        .fullName("Alex Brown")
                        .build(),
                    Colleague.builder()
                        .role(ColleagueRole.SOLICITOR)
                        .fullName("Emma White")
                        .build()))
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Select case role for colleague 1",
            "Enter email for colleague 1",
            "Select send them case update notifications for colleague 1",
            "Enter email for colleague 2",
            "Select send them case update notifications for colleague 2"
        );
    }

    private LocalAuthority getPopulatedLocalAuthority() {
        return LocalAuthority.builder()
            .name("Org")
            .email("org@test.com")
            .legalTeamManager("John Smith")
            .pbaNumber("PBA1234567")
            .address(Address.builder()
                .postcode("AB 1CD")
                .addressLine1("Line 1")
                .build())
            .phone("0777777777")
            .colleagues(wrapElements(Colleague.builder()
                .role(ColleagueRole.SOLICITOR)
                .fullName("Alex Brown")
                .email("test@test.com")
                .notificationRecipient("Yes")
                .build()))
            .build();
    }
}
