package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.ChildParty.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ChildrenCheckerIsStartedTest {

    @InjectMocks
    private ChildrenChecker childrenChecker;

    @Test
    void shouldReturnFalseWhenNoChildren() {
        final CaseData caseData = CaseData.builder().build();

        final boolean isStarted = childrenChecker.isStarted(caseData);

        assertThat(isStarted).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoChildrenDetailsProvided() {
        final Child child = Child.builder()
                .party(builder().build())
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(wrapElements(child))
                .build();

        final boolean isStarted = childrenChecker.isStarted(caseData);

        assertThat(isStarted).isFalse();
    }

    @Test
    void shouldReturnFalseWhenEmptyChildrenDetailsProvided() {
        final Child child = Child.builder()
                .party(builder()
                        .firstName("")
                        .lastName("")
                        .dateOfBirth(null)
                        .gender(null)
                        .livingSituation("")
                        .keyDates("")
                        .careAndContactPlan("")
                        .adoption("")
                        .mothersName("")
                        .fathersName("")
                        .fathersResponsibility("")
                        .socialWorkerName("")
                        .socialWorkerEmail("")
                        .additionalNeeds("")
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
                        .socialWorkerTelephoneNumber(Telephone.builder()
                                .telephoneUsageType("")
                                .contactDirection("")
                                .telephoneNumber("")
                                .build())
                        .build())
                .build();

        final CaseData caseData = CaseData.builder()
                .children1(wrapElements(child))
                .build();

        final boolean isStarted = childrenChecker.isStarted(caseData);

        assertThat(isStarted).isFalse();
    }

    @Test
    void shouldReturnTrueWhenMoreThanOneChildrenProvided() {
        final CaseData caseData = CaseData.builder()
                .children1(wrapElements(Child.builder().build(), Child.builder().build()))
                .build();

        final boolean isStarted = childrenChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyChildren")
    void shouldReturnTrueWhenAtLeastOneChildDetailsProvided(ChildParty childParty) {
        final Child child = Child.builder()
                .party(childParty)
                .build();
        final CaseData caseData = CaseData.builder()
                .children1(wrapElements(child))
                .build();

        final boolean isStarted = childrenChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    private static Stream<Arguments> nonEmptyChildren() {
        return Stream.of(
                ChildParty.builder().firstName("Test").build(),
                ChildParty.builder().lastName("Test").build(),
                ChildParty.builder().dateOfBirth(LocalDate.now()).build(),
                ChildParty.builder().gender(ChildGender.BOY).build(),
                ChildParty.builder().livingSituation("Test").build(),
                ChildParty.builder().keyDates("Test").build(),
                ChildParty.builder().careAndContactPlan("Test").build(),
                ChildParty.builder().adoption("Test").build(),
                ChildParty.builder().mothersName("Test").build(),
                ChildParty.builder().fathersName("Test").build(),
                ChildParty.builder().fathersResponsibility("Test").build(),
                ChildParty.builder().socialWorkerName("Test").build(),
                ChildParty.builder().socialWorkerEmail("Test").build(),
                ChildParty.builder().additionalNeeds("Test").build(),
                ChildParty.builder().litigationIssues("Test").build(),
                ChildParty.builder().address(
                        Address.builder().addressLine1("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().addressLine2("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().addressLine3("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().postTown("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().county("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().country("Test").build()).build(),
                ChildParty.builder().address(
                        Address.builder().postcode("Test").build()).build(),
                ChildParty.builder().socialWorkerTelephoneNumber(
                        Telephone.builder().telephoneUsageType("Test").build()).build(),
                ChildParty.builder().socialWorkerTelephoneNumber(
                        Telephone.builder().telephoneNumber("Test").build()).build(),
                ChildParty.builder().socialWorkerTelephoneNumber(
                        Telephone.builder().contactDirection("Test").build()).build())
                .map(Arguments::of);
    }
}
