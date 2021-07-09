package uk.gov.hmcts.reform.fpl.model.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.CHILD_ARRANGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.SPECIFIC_ISSUE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32A_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C33_INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.CHILDREN_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ISSUING_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ORDER_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.OTHER_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.REVIEW;

class OrderTest {

    @Test
    void fileExtension() {
        assertThat(C21_BLANK_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c21_blank_order.pdf");
        assertThat(C21_BLANK_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c21_blank_order.doc");
        assertThat(C32A_CARE_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c32a_care_order.pdf");
        assertThat(C32A_CARE_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c32a_care_order.doc");
        assertThat(C32B_DISCHARGE_OF_CARE_ORDER.fileName(RenderFormat.PDF)).isEqualTo(
            "c32b_discharge_of_care_order.pdf");
        assertThat(C32B_DISCHARGE_OF_CARE_ORDER.fileName(RenderFormat.WORD)).isEqualTo(
            "c32b_discharge_of_care_order.doc");
        assertThat(C35A_SUPERVISION_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c35a_supervision_order.pdf");
        assertThat(C35A_SUPERVISION_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c35a_supervision_order.doc");
        assertThat(C33_INTERIM_CARE_ORDER.fileName(RenderFormat.PDF)).isEqualTo("c33_interim_care_order.pdf");
        assertThat(C33_INTERIM_CARE_ORDER.fileName(RenderFormat.WORD)).isEqualTo("c33_interim_care_order.doc");
        assertThat(C35B_INTERIM_SUPERVISION_ORDER.fileName(RenderFormat.PDF))
            .isEqualTo("c35b_interim_supervision_order.pdf");
        assertThat(C35B_INTERIM_SUPERVISION_ORDER.fileName(RenderFormat.WORD))
            .isEqualTo("c35b_interim_supervision_order.doc");
        assertThat(C23_EMERGENCY_PROTECTION_ORDER.fileName(RenderFormat.PDF))
            .isEqualTo("c23_emergency_protection_order.pdf");
        assertThat(C23_EMERGENCY_PROTECTION_ORDER.fileName(RenderFormat.WORD))
            .isEqualTo("c23_emergency_protection_order.doc");
        assertThat(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.fileName(RenderFormat.PDF))
            .isEqualTo("c47a_appointment_of_a_childrens_guardian.pdf");
        assertThat(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.fileName(RenderFormat.WORD))
            .isEqualTo("c47a_appointment_of_a_childrens_guardian.doc");
        assertThat(C45A_PARENTAL_RESPONSIBILITY_ORDER.fileName(RenderFormat.WORD))
            .isEqualTo("c45b_parental_responsibility_order.doc");
        assertThat(C45A_PARENTAL_RESPONSIBILITY_ORDER.fileName(RenderFormat.PDF))
            .isEqualTo("c45b_parental_responsibility_order.pdf");
    }

    @ParameterizedTest
    @MethodSource("c43OrdersSource")
    void c43ChildArrangementSpecificIssueProhibitedStepsOrderFileExtension(List<C43OrderType> orders,
                                                                        String expectedString) {
        Order order = C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersMultiSelectListForC43(orders)
            .build();

        String expectedPdfString = expectedString + ".pdf";
        String expectedWordString = expectedString + ".doc";

        assertThat(order.fileName(RenderFormat.PDF, manageOrdersEventData)).isEqualTo(expectedPdfString);
        assertThat(order.fileName(RenderFormat.WORD, manageOrdersEventData)).isEqualTo(expectedWordString);
    }

    @Test
    void firstSection() {
        assertThat(C21_BLANK_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C23_EMERGENCY_PROTECTION_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C32A_CARE_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER.firstSection())
            .isEqualTo(HEARING_DETAILS);
        assertThat(C32B_DISCHARGE_OF_CARE_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C33_INTERIM_CARE_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C35A_SUPERVISION_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C35B_INTERIM_SUPERVISION_ORDER.firstSection()).isEqualTo(HEARING_DETAILS);
        assertThat(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.firstSection()).isEqualTo(HEARING_DETAILS);
    }

    @ParameterizedTest
    @MethodSource("sectionsWithNext")
    void testNextSection(Order order, OrderSection currentSection, Optional<OrderSection> expectedNextSection) {
        assertThat(order.nextSection(currentSection)).isEqualTo(expectedNextSection);
    }

    @ParameterizedTest
    @MethodSource("sectionsWithNextForManualUploads")
    void testNextSectionForManualUploads(Order order, OrderSection currentSection,
                                         Optional<OrderSection> expectedNextSection) {
        assertThat(order.nextSection(currentSection)).isEqualTo(expectedNextSection);
    }

    @Test
    void checkCoverage() {
        Set<Order> allOrders = Arrays.stream(Order.values())
            .filter(order -> OrderSourceType.DIGITAL == order.getSourceType())
            .collect(Collectors.toSet());

        Set<Order> testedOrders = sectionsWithNext()
            .map(arguments -> (Order) arguments.get()[0])
            .collect(Collectors.toSet());

        assertThat(testedOrders).isEqualTo(allOrders);
    }

    private static Stream<Arguments> c43OrdersSource() {
        return Stream.of(
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER), "c43_child_arrangements"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER), "c43_specific_issue"),
            Arguments.of(List.of(PROHIBITED_STEPS_ORDER), "c43_prohibited_steps"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER),
                "c43_child_arrangements_specific_issue"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, PROHIBITED_STEPS_ORDER),
                "c43_child_arrangements_prohibited_steps"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "c43_specific_issue_prohibited_steps"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "c43_child_arrangements_specific_issue_prohibited_steps")
        );
    }

    private static Stream<Arguments> sectionsWithNext() {
        return Stream.of(
            Arguments.of(C21_BLANK_ORDER, HEARING_DETAILS, Optional.of(ISSUING_DETAILS)),
            Arguments.of(C21_BLANK_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C21_BLANK_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C21_BLANK_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C21_BLANK_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C21_BLANK_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, HEARING_DETAILS, Optional.of(ISSUING_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C32A_CARE_ORDER, HEARING_DETAILS, Optional.of(ISSUING_DETAILS)),
            Arguments.of(C32A_CARE_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C32A_CARE_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C32A_CARE_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C32A_CARE_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C32A_CARE_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                HEARING_DETAILS,
                Optional.of(ISSUING_DETAILS)),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                ISSUING_DETAILS,
                Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                CHILDREN_DETAILS,
                Optional.of(ORDER_DETAILS)),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                ORDER_DETAILS,
                Optional.of(REVIEW)),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                REVIEW,
                Optional.of(OTHER_DETAILS)),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                OTHER_DETAILS,
                Optional.empty()),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, HEARING_DETAILS, Optional.of(ISSUING_DETAILS)),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C33_INTERIM_CARE_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C33_INTERIM_CARE_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C33_INTERIM_CARE_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C33_INTERIM_CARE_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C33_INTERIM_CARE_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C35A_SUPERVISION_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C35A_SUPERVISION_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C35A_SUPERVISION_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C35A_SUPERVISION_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C35A_SUPERVISION_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C35B_INTERIM_SUPERVISION_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C35B_INTERIM_SUPERVISION_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C35B_INTERIM_SUPERVISION_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C35B_INTERIM_SUPERVISION_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C35B_INTERIM_SUPERVISION_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, OTHER_DETAILS, Optional.empty()),
            Arguments.of(C45A_PARENTAL_RESPONSIBILITY_ORDER, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
            Arguments.of(C45A_PARENTAL_RESPONSIBILITY_ORDER, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C45A_PARENTAL_RESPONSIBILITY_ORDER, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C45A_PARENTAL_RESPONSIBILITY_ORDER, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, ISSUING_DETAILS, Optional.of(ORDER_DETAILS)),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, ORDER_DETAILS, Optional.of(REVIEW)),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, REVIEW, Optional.of(OTHER_DETAILS)),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, OTHER_DETAILS, Optional.empty())
        );
    }

    private static Stream<Arguments> sectionsWithNextForManualUploads() {
        return Arrays.stream(Order.values())
            .filter(order -> OrderSourceType.MANUAL_UPLOAD == order.getSourceType())
            .flatMap(order -> Stream.of(
                Arguments.of(order, ISSUING_DETAILS, Optional.of(CHILDREN_DETAILS)),
                Arguments.of(order, CHILDREN_DETAILS, Optional.of(ORDER_DETAILS)),
                Arguments.of(order, ORDER_DETAILS, Optional.of(REVIEW)),
                Arguments.of(order, REVIEW, Optional.of(OTHER_DETAILS)))
            );
    }
}
