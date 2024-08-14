package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.GroundsList;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.GroundsForRefuseContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForSecureAccommodationOrder;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GroundsCheckerIsStartedTest {

    @InjectMocks
    private GroundsChecker groundsChecker;

    @ParameterizedTest
    @MethodSource("emptyGrounds")
    void shouldReturnFalseWhenEmptyGrounds(Grounds grounds, GroundsForEPO groundsForEPO) {
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .groundsForEPO(groundsForEPO)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyGrounds")
    void shouldReturnTrueWhenGroundsProvided(Grounds grounds) {
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyGroundsForEPO")
    void shouldReturnTrueWhenGroundsForEPOProvided(GroundsForEPO groundsForEPO) {
        final CaseData caseData = CaseData.builder()
                .groundsForEPO(groundsForEPO)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenGroundsForEPOProvided() {
        final CaseData caseData = CaseData.builder()
            .groundsForSecureAccommodationOrder(GroundsForSecureAccommodationOrder.builder()
                .reasonAndLength("some reason").build())
            .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("emptyGroundsForRefuseContact")
    void shouldReturnTrueWhenGroundsForRefuseContactProvided(GroundsForRefuseContactWithChild grounds) {
        final CaseData caseData = CaseData.builder()
            .groundsForRefuseContactWithChild(grounds)
            .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmptyGroundsForRefuseContact() {
        final CaseData caseData = CaseData.builder()
            .groundsForRefuseContactWithChild(GroundsForRefuseContactWithChild.builder().build())
            .build();

        assertThat(groundsChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("emptyGroundsForContactWithChildInCare")
    void shouldReturnTrueWhenGroundsForContactWithChildProvided(GroundsForContactWithChild grounds) {
        final CaseData caseData = CaseData.builder()
            .groundsForContactWithChild(grounds)
            .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenGroundsForContactWithChildNotProvided() {
        final CaseData caseData = CaseData.builder()
            .groundsForContactWithChild(GroundsForContactWithChild.builder().build())
            .build();

        assertThat(groundsChecker.isStarted(caseData)).isFalse();
    }

    private static Stream<Arguments> nonEmptyGrounds() {
        return Stream.of(
                Grounds.builder().groundsReason(List.of((GroundsList.NO_CARE))).build(),
                Grounds.builder().thresholdDetails("Test").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> nonEmptyGroundsForEPO() {
        return Stream.of(
                GroundsForEPO.builder().reason(List.of("Test")).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyGrounds() {
        return Stream.of(
                Arguments.of(
                        Grounds.builder().build(),
                        GroundsForEPO.builder().build()),
                Arguments.of(
                        Grounds.builder()
                                .thresholdDetails("")
                                .groundsReason(emptyList())
                                .build(),
                        GroundsForEPO.builder()
                                .reason(emptyList())
                                .build()));
    }

    private static Stream<Arguments> emptyGroundsForRefuseContact() {
        return Stream.of(
            Arguments.of(GroundsForRefuseContactWithChild.builder().laHasRefusedContact("1").build()),
            Arguments.of(GroundsForRefuseContactWithChild.builder().personHasContactAndCurrentArrangement("1").build()),
            Arguments.of(GroundsForRefuseContactWithChild.builder().reasonsOfApplication("1").build()),
            Arguments.of(GroundsForRefuseContactWithChild.builder().personsBeingRefusedContactWithChild("1").build()));
    }

    private static Stream<Arguments> emptyGroundsForContactWithChildInCare() {
        return Stream.of(
            Arguments.of(GroundsForContactWithChild.builder().parentOrGuardian("1").build()),
            Arguments.of(GroundsForContactWithChild.builder().residenceOrder("1").build()),
            Arguments.of(GroundsForContactWithChild.builder().hadCareOfChildrenBeforeCareOrder("1").build()),
            Arguments.of(GroundsForContactWithChild.builder().reasonsForApplication("1").build()));
    }
}
