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

import static org.apache.commons.lang3.BooleanUtils.NO;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class LocalAuthorityDetailsCheckerTest {

    private LocalAuthorityDetailsChecker underTest = new LocalAuthorityDetailsChecker();

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorMessageWhenLocalAuthoritiesDetailsNotAdded(List<Element<LocalAuthority>> localAuthorities) {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities)
            .build();

        assertThat(underTest.validate(caseData)).containsExactly("Add applicant's details");
    }

    @Test
    void shouldReturnErrorsWhenLocalAuthoritiesDetailsNotPresent() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder().build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Enter applicant's name",
            "Enter applicant's pba number",
            "Enter applicant's customer reference",
            "Enter applicant's address",
            "Enter main contact"
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
            "Enter applicant's postcode",
            "Enter valid applicant's address"
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
                    .firstName("Alex")
                    .lastName("Brown")
                    .email("test@test.com")
                    .phone("123456789")
                    .mainContact(YES.getValue())
                    .notificationRecipient(YES.getValue())
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
                .containsExactly("Enter main contact");
    }

    @Test
    void shouldReturnErrorsForSingleColleague() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                .toBuilder()
                .colleagues(wrapElements(Colleague.builder()
                    .firstName("Alex")
                    .lastName("Brown")
                    .mainContact(NO)
                    .build()))
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Enter main contact",
            "Select case role for other contact 1",
            "Enter email for other contact 1"
        );
    }

    @Test
    void shouldReturnErrorsForMultipleColleagues() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(getPopulatedLocalAuthority()
                .toBuilder()
                .colleagues(wrapElements(
                    Colleague.builder()
                        .firstName("Alex")
                        .lastName("Brown")
                        .build(),
                    Colleague.builder()
                        .role(ColleagueRole.SOLICITOR)
                        .firstName("Emma")
                        .lastName("White")
                        .build()))
                .build()))
            .build();

        assertThat(underTest.validate(caseData)).containsExactly(
            "Enter main contact",
            "Select case role for other contact 1",
            "Enter email for other contact 1",
            "Enter email for other contact 2"
        );
    }

    private LocalAuthority getPopulatedLocalAuthority() {
        return LocalAuthority.builder()
            .name("Org")
            .email("org@test.com")
            .legalTeamManager("John Smith")
            .pbaNumber("PBA1234567")
            .customerReference("customerReference")
            .address(Address.builder()
                .postcode("AB 1CD")
                .addressLine1("Line 1")
                .build())
            .phone("0777777777")
            .colleagues(wrapElements(Colleague.builder()
                .role(ColleagueRole.SOLICITOR)
                .firstName("Alex")
                .lastName("Brown")
                .email("test@test.com")
                .phone("123456789")
                .notificationRecipient(YES.getValue())
                .mainContact(YES.getValue())
                .build()))
            .build();
    }
}
