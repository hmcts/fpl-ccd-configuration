package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.RespondentParty.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondentsCheckerIsStartedTest {

    @InjectMocks
    private RespondentsChecker respondentsChecker;

    @ParameterizedTest
    @MethodSource("emptyRespondents")
    void shouldReturnFalseWhenEmptyRespondents(Respondent respondent) {
        final CaseData caseData = CaseData.builder()
                .respondents1(wrapElements(respondent))
                .build();

        assertThat(respondentsChecker.isStarted(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenMoreThanOneChildrenProvided() {
        final CaseData caseData = CaseData.builder()
                .respondents1(wrapElements(Respondent.builder().build(), Respondent.builder().build()))
                .build();

        assertThat(respondentsChecker.isStarted(caseData)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyRespondent")
    void shouldReturnTrueWhenNonEmptyRespondent(RespondentParty respondentParty) {
        final Respondent respondent = Respondent.builder()
                .party(respondentParty)
                .build();
        final CaseData caseData = CaseData.builder()
                .respondents1(wrapElements(respondent))
                .build();

        assertThat(respondentsChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyRespondent() {
        return Stream.of(
                builder().firstName("Test").build(),
                builder().lastName("Test").build(),
                builder().dateOfBirth(LocalDate.now()).build(),
                builder().gender("Boy").build(),
                builder().relationshipToChild("Test").build(),
                builder().contactDetailsHidden("No").build(),
                builder().litigationIssues("Test").build(),
                builder().address(Address.builder().addressLine1("Test").build()).build(),
                builder().address(Address.builder().addressLine2("Test").build()).build(),
                builder().address(Address.builder().addressLine3("Test").build()).build(),
                builder().address(Address.builder().postTown("Test").build()).build(),
                builder().address(Address.builder().county("Test").build()).build(),
                builder().address(Address.builder().country("Test").build()).build(),
                builder().address(Address.builder().postcode("Test").build()).build(),
                builder().telephoneNumber(Telephone.builder().telephoneUsageType("Test").build()).build(),
                builder().telephoneNumber(Telephone.builder().telephoneNumber("Test").build()).build(),
                builder().email(EmailAddress.builder().email("Test@test.com").build()).build(),
                builder().email(EmailAddress.builder().emailUsageType("Test").build()).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyRespondents() {
        return Stream.of(
                Respondent.builder()
                        .build(),
                Respondent.builder()
                        .party(RespondentParty.builder().build())
                        .build(),
                Respondent.builder()
                        .party(RespondentParty.builder()
                                .firstName("")
                                .lastName("")
                                .dateOfBirth(null)
                                .gender("")
                                .placeOfBirth("")
                                .relationshipToChild("")
                                .contactDetailsHidden("")
                                .litigationIssues("")
                                .address(Address.builder()
                                        .addressLine1("")
                                        .addressLine2("")
                                        .addressLine3("")
                                        .county("")
                                        .country("")
                                        .postTown("")
                                        .postcode("")
                                        .build())
                                .email(EmailAddress.builder()
                                        .email("")
                                        .emailUsageType("")
                                        .build())
                                .telephoneNumber(Telephone.builder()
                                        .telephoneUsageType("")
                                        .contactDirection("")
                                        .telephoneNumber("")
                                        .build())
                                .build())
                        .build())
                .map(Arguments::of);
    }
}
