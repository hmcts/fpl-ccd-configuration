package uk.gov.hmcts.reform.fnp.model.fee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.Supplements;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PARENTAL_RESPONSIBILITY_FATHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PARENTAL_RESPONSIBILITY_FEMALE_PARENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SECURE_ACCOMMODATION_ENGLAND;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SECURE_ACCOMMODATION_WALES;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2ApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2OrdersRequestedType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromOrderType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromParentalResponsibilityTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSecureAccommodationTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSupplementTypes;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested.CHANGE_SURNAME_OR_REMOVE_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C100_CHILD_ARRANGEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C17A_EXTENSION_OF_ESO;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C17_EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C19_WARRANT_TO_ASSISTANCE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_SECOND_FEMALE_PARENT;

class FeeTypeTest {
    private static Stream<Arguments> orderToFeeTypeSource() {
        // Will throw an IllegalArgumentException if there is no corresponding FeeType
        return Arrays.stream(OrderType.values())
            .map(orderType -> Arguments.of(List.of(orderType), List.of(FeeType.valueOf(orderType.name()))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenNullOrEmptyListIsPassed(List<OrderType> list) {
        assertThat(fromOrderType(list)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("orderToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForOrderType(List<OrderType> orderType, List<FeeType> feeType) {
        assertThat(fromOrderType(orderType)).isEqualTo(feeType);
    }

    @Test
    void shouldReturnCorrespondingFeeTypeForC2ApplicationType() {
        assertThat(fromC2ApplicationType(WITH_NOTICE)).isEqualTo(C2_WITH_NOTICE);
        assertThat(fromC2ApplicationType(WITHOUT_NOTICE)).isEqualTo(C2_WITHOUT_NOTICE);
    }

    @ParameterizedTest
    @MethodSource("c2RequestedOrderToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForC2RequestedOrdersType(
        C2OrdersRequested c2OrderRequested, FeeType feeType) {
        assertThat(fromC2OrdersRequestedType(List.of(c2OrderRequested))).isEqualTo(List.of(feeType));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenC2RequestedOrderIsNullOrEmptyList(List<C2OrdersRequested> list) {
        assertThat(fromC2OrdersRequestedType(list)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("otherApplicationTypeToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForOtherApplicationsType(OtherApplicationType type, FeeType feeType) {
        assertThat(fromApplicationType(type)).hasValue(feeType);
    }

    @ParameterizedTest
    @EnumSource(value = OtherApplicationType.class,
        names = {"C1_PARENTAL_RESPONSIBILITY", "C1_WITH_SUPPLEMENT", "C4_WHEREABOUTS_OF_A_MISSING_CHILD"})
    void shouldReturnEmptyWhenFeeTypeDoesNotExistOtherApplicationType(OtherApplicationType type) {
        assertThat(fromApplicationType(type)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("supplementTypeToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForSupplementType(Supplements supplementType, List<FeeType> feeTypes) {
        assertThat(fromSupplementTypes(List.of(supplementType))).isEqualTo(feeTypes);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenSupplementTypeIsNullOrEmptyList(List<Supplements> supplementsList) {
        assertThat(fromSupplementTypes(supplementsList)).isEmpty();
    }

    @Test
    void shouldReturnCorrespondingFeeTypeForParentalResponsibilityType() {
        assertThat(fromParentalResponsibilityTypes(PR_BY_SECOND_FEMALE_PARENT))
            .isEqualTo(PARENTAL_RESPONSIBILITY_FEMALE_PARENT);
        assertThat(fromParentalResponsibilityTypes(PR_BY_FATHER)).isEqualTo(PARENTAL_RESPONSIBILITY_FATHER);
    }

    @Test
    void shouldReturnCorrespondingFeeTypeForSecureAccommodationType() {
        assertThat(fromSecureAccommodationTypes(List.of(SecureAccommodationType.ENGLAND)))
            .isEqualTo(List.of(SECURE_ACCOMMODATION_ENGLAND));
        assertThat(fromSecureAccommodationTypes(List.of(SecureAccommodationType.WALES)))
            .isEqualTo(List.of(SECURE_ACCOMMODATION_WALES));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenSecureAccommodationTypeListIsNullOrEmptyList(List<SecureAccommodationType> types) {
        assertThat(fromSecureAccommodationTypes(types)).isEmpty();
    }

    private static Stream<Arguments> c2RequestedOrderToFeeTypeSource() {
        return Stream.of(
            Arguments.of(CHANGE_SURNAME_OR_REMOVE_JURISDICTION, FeeType.CHANGE_SURNAME),
            Arguments.of(C2OrdersRequested.APPOINTMENT_OF_GUARDIAN, FeeType.APPOINTMENT_OF_GUARDIAN),
            Arguments.of(C2OrdersRequested.TERMINATION_OF_APPOINTMENT_OF_GUARDIAN, FeeType.APPOINTMENT_OF_GUARDIAN)
        );
    }

    private static Stream<Arguments> otherApplicationTypeToFeeTypeSource() {
        return Stream.of(
            Arguments.of(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, FeeType.CHANGE_SURNAME),
            Arguments.of(C1_APPOINTMENT_OF_A_GUARDIAN, FeeType.APPOINTMENT_OF_GUARDIAN),
            Arguments.of(C1_TERMINATION_OF_APPOINTMENT_OF_A_GUARDIAN, FeeType.APPOINTMENT_OF_GUARDIAN),
            Arguments.of(C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD, FeeType.C2_WITH_NOTICE),
            Arguments.of(C12_WARRANT_TO_ASSIST_PERSON, FeeType.WARRANT_TO_ASSIST_PERSON),
            Arguments.of(C17_EDUCATION_SUPERVISION_ORDER, FeeType.EDUCATION_SUPERVISION_ORDER),
            Arguments.of(C17A_EXTENSION_OF_ESO, FeeType.ESO_EXTENSION),
            Arguments.of(C19_WARRANT_TO_ASSISTANCE, FeeType.WARRANT_OF_ASSISTANCE),
            Arguments.of(C100_CHILD_ARRANGEMENTS, FeeType.CHILD_ARRANGEMENTS)
        );
    }

    private static Stream<Arguments> supplementTypeToFeeTypeSource() {
        return Stream.of(
            Arguments.of(Supplements.C13A_SPECIAL_GUARDIANSHIP, List.of(FeeType.SPECIAL_GUARDIANSHIP)),
            Arguments.of(
                Supplements.C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD, List.of(FeeType.CONTACT_WITH_CHILD_IN_CARE)),
            Arguments.of(Supplements.C15_CONTACT_WITH_CHILD_IN_CARE, List.of(FeeType.CONTACT_WITH_CHILD_IN_CARE)),
            Arguments.of(Supplements.C16_CHILD_ASSESSMENT, List.of(FeeType.CHILD_ASSESSMENT)),
            Arguments.of(Supplements.C18_RECOVERY_ORDER, List.of(FeeType.RECOVERY_ORDER))
        );
    }

}
