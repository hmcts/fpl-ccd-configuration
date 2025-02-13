package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderGround;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForChildAssessmentOrder;
import uk.gov.hmcts.reform.fpl.model.GroundsForContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.GroundsForEducationSupervisionOrder;
import uk.gov.hmcts.reform.fpl.model.GroundsForRefuseContactWithChild;
import uk.gov.hmcts.reform.fpl.model.GroundsForSecureAccommodationOrder;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GroundsChecker.class, LocalValidatorFactoryBean.class})
class GroundsCheckerTest {

    @Autowired
    private GroundsChecker groundsChecker;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnErrorWhenNoGroundsForApplication() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).contains("Add the grounds for the application");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNoGroundsForApplicationDetails() {
        final Grounds grounds = Grounds.builder().build();
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
                "Select at least one option for how this case meets the threshold criteria",
                "Enter details of how the case meets the threshold criteria"
        );
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsForApplicationAreProvided() {
        final Grounds grounds = Grounds.builder()
                .thresholdReason(List.of("Beyond parental control"))
                .thresholdDetails("Custom details")
                .build();
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorWhenEpoOrderRequestedButNoGroundsProvided() {
        final Grounds grounds = Grounds.builder()
                .thresholdReason(List.of("Beyond parental control"))
                .thresholdDetails("Custom details")
                .build();
        final CaseData caseData = CaseData.builder()
                .orders(Orders.builder()
                        .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                        .build())
                .grounds(grounds)
                .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder("Add the grounds for the application");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForRequestedEpoOrder() {
        final Grounds grounds = Grounds.builder()
                .thresholdReason(List.of("Beyond parental control"))
                .thresholdDetails("Custom details")
                .build();
        final GroundsForEPO groundsForEPO = GroundsForEPO.builder()
                .reason(List.of("Child is likely to suffer harm if they don't stay in their current accommodation"))
                .build();
        final CaseData caseData = CaseData.builder()
                .orders(Orders.builder()
                        .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                        .build())
                .grounds(grounds)
                .groundsForEPO(groundsForEPO)
                .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForRequestedChildAssessmentOrder() {
        final GroundsForChildAssessmentOrder grounds = GroundsForChildAssessmentOrder.builder()
            .thresholdDetails("Custom details")
            .build();
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.CHILD_ASSESSMENT_ORDER))
                .build())
            .groundsForChildAssessmentOrder(grounds)
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorWhenChildAssessmentOrderRequestedButNoGroundsProvided() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.CHILD_ASSESSMENT_ORDER))
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder("Add the grounds for the application");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForSecureAccommodationOrder() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER)).build())
            .groundsForSecureAccommodationOrder(GroundsForSecureAccommodationOrder.builder()
                .grounds(List.of(SecureAccommodationOrderGround.ABSCOND_FROM_ACCOMMODATION,
                    SecureAccommodationOrderGround.SELF_INJURY))
                .reasonAndLength("No reason and 2 days")
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorsWhenGroundsNotProvidedForSecureAccommodationOrder() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.SECURE_ACCOMMODATION_ORDER)).build())
            .groundsForSecureAccommodationOrder(GroundsForSecureAccommodationOrder.builder()
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Please give reasons for the application and length of the order sought",
            "Select at least one option for how this case meets grounds for a secure accommodation order");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForRefuseContactWithChild() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.REFUSE_CONTACT_WITH_CHILD)).build())
            .groundsForRefuseContactWithChild(GroundsForRefuseContactWithChild.builder()
                .personHasContactAndCurrentArrangement("test1")
                .laHasRefusedContact("test2")
                .personsBeingRefusedContactWithChild("test3")
                .reasonsOfApplication("test4")
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorsWhenGroundsNotProvidedForRefuseContactWithChild() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.REFUSE_CONTACT_WITH_CHILD)).build())
            .groundsForRefuseContactWithChild(GroundsForRefuseContactWithChild.builder()
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Please state the full name(s) of each person who has contact with each child "
                + "and the current arrangements for contact",
            "Please state whether the local authority has refused contact for 7 days or less",
            "Please state the full name and relationship of any person in respect of whom authority to "
                + "refuse contact with each child is sought",
            "Please provide reasons for application");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForContactWithChildInCare() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.CONTACT_WITH_CHILD_IN_CARE)).build())
            .groundsForContactWithChild(GroundsForContactWithChild.builder()
                .parentOrGuardian("test1")
                .residenceOrder("test2")
                .hadCareOfChildrenBeforeCareOrder("test3")
                .reasonsForApplication("test4")
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorsWhenGroundsNotProvidedForContactWithChildInCare() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.CONTACT_WITH_CHILD_IN_CARE)).build())
            .groundsForContactWithChild(GroundsForContactWithChild.builder()
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Please state whether you are a parent or a guardian",
            "Please state whether you hold a residence order which was in force "
                + "immediately before the care order was made (Section 34(1)(c) Children Act 1989)",
            "Please state whether you had care of the child(ren) through an order which was in force "
                + "immediately before the care order was made (Section 34(1)(d) Children Act 1989)",
            "Please provide reasons for application");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForEducationSupervisionOrder() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder().orderType(List.of(OrderType.EDUCATION_SUPERVISION_ORDER)).build())
            .groundsForEducationSupervisionOrder(GroundsForEducationSupervisionOrder.builder()
                .groundDetails("ground details").build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    @Test
    void shouldReturnErrorWhenEducationSupervisionOrderRequestedButNoGroundsProvided() {
        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .build();

        final List<String> errors = groundsChecker.validate(caseData);
        final boolean isCompleted = groundsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder("Add the grounds for the application");
        assertThat(isCompleted).isFalse();
    }

}
