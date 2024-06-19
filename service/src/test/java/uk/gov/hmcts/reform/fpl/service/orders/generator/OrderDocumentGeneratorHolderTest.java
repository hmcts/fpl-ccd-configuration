package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A81_PLACEMENT_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C26_SECURE_ACCOMMODATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C29_RECOVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32A_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C33_INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34B_AUTHORITY_TO_REFUSE_CONTACT;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C39_CHILD_ASSESSMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C42_FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C44A_LEAVE_TO_CHANGE_A_SURNAME;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C63A_DECLARATION_OF_PARENTAGE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.FL404A_NON_MOLESTATION_ORDER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDocumentGeneratorHolderTest {

    private List<DocmosisParameterGenerator> generators;
    private List<AdditionalDocumentsCollector> collectors;
    private Map<Order, DocmosisParameterGenerator> typeToGenerator;
    private Map<Order, AdditionalDocumentsCollector> typeToAdditionalDocsCollector;

    // Parameter Generators
    @Mock
    private A70PlacementOrderDocumentParameterGenerator a70PlacementOrderDocumentParameterGenerator;
    @Mock
    private C21BlankOrderDocumentParameterGenerator c21BlankOrderDocumentParameterGenerator;
    @Mock
    private A81PlacementBlankOrderDocumentParameterGenerator a81PlacementBlankOrderDocumentParameterGenerator;
    @Mock
    private C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;
    @Mock
    private C26SecureAccommodationOrderDocumentParameterGenerator c26SecureAccommodationOrderDocumentParameterGenerator;
    @Mock
    private C29RecoveryOfAChildDocumentParameterGenerator c29RecoveryOfAChildDocumentParameterGenerator;
    @Mock
    private C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;
    @Mock
    private C32bDischargeOfCareOrderDocumentParameterGenerator c32bDischargeOfCareOrderDocumentParameterGenerator;
    @Mock
    private C33InterimCareOrderDocumentParameterGenerator c33InterimCareOrderDocumentParameterGenerator;
    @Mock
    private C34aContactWithAChildInCareOrderDocumentParameterGenerator
        c34AContactWithAChildInCareOrderDocumentParameterGenerator;
    @Mock
    private C34BAuthorityToRefuseContactOrderParameterGenerator c34BAuthorityToRefuseContactOrderParameterGenerator;
    @Mock
    private C35aSupervisionOrderDocumentParameterGenerator c35aSupervisionOrderDocumentParameterGenerator;
    @Mock
    private C35bISODocumentParameterGenerator c35bISODocumentParameterGenerator;
    @Mock
    private C39ChildAssessmentOrderParameterGenerator c39ChildAssessmentOrderParameterGenerator;
    @Mock
    private C36VariationOrExtensionOfSupervisionOrdersParameterGenerator
        c36VariationOrExtensionOfSupervisionOrdersParameterGenerator;
    @Mock
    private C37EducationSupervisionOrderParameterGenerator c37EducationSupervisionOrderParameterGenerator;
    @Mock
    private C43aSpecialGuardianshipOrderDocumentParameterGenerator c43aSGODocumentParameterGenerator;
    @Mock
    private C43ChildArrangementOrderDocumentParameterGenerator c43ChildArrangementOrderDocumentParameterGenerator;
    @Mock
    private C44aLeaveToChangeTheSurnameOrderParameterGenerator c44aLeaveToChangeTheSurnameOrderParameterGenerator;
    @Mock
    private C47AAppointmentOfAChildrensGuardianParameterGenerator c47AAppointmentOfAChildrensGuardianParameterGenerator;
    @Mock
    private C42FamilyAssistanceOrderDocumentParameterGenerator c42FamilyAssistanceOrderDocumentParameterGenerator;
    @Mock
    private NonMolestationOrderDocumentParameterGenerator nonMolestationOrderDocumentParameterGenerator;
    @Mock
    private C45aParentalResponsibilityOrderDocumentParameterGenerator
        c45aParentalResponsibilityOrderDocumentParameterGenerator;
    @Mock
    private C63aDeclarationOfParentageDocumentParameterGenerator
        c63aDeclarationOfParentageDocumentParameterGenerator;

    // Additional Document Collectors
    @Mock
    private C23EPOAdditionalDocumentsCollector c23EPOAdditionalDocumentsCollector;

    // Notification document generators
    @Mock
    private A206PlacementOrderNotificationParameterGenerator a206PlacementOrderNotificationParameterGenerator;

    @InjectMocks
    private OrderDocumentGeneratorHolder underTest;

    @BeforeEach
    void setUp() {
        generators = List.of(
            a70PlacementOrderDocumentParameterGenerator, a81PlacementBlankOrderDocumentParameterGenerator,
            c21BlankOrderDocumentParameterGenerator, c23EPODocumentParameterGenerator,
            c26SecureAccommodationOrderDocumentParameterGenerator, c29RecoveryOfAChildDocumentParameterGenerator,
            c32CareOrderDocumentParameterGenerator, c32bDischargeOfCareOrderDocumentParameterGenerator,
            c33InterimCareOrderDocumentParameterGenerator, c35aSupervisionOrderDocumentParameterGenerator,
            c37EducationSupervisionOrderParameterGenerator, c47AAppointmentOfAChildrensGuardianParameterGenerator,
            c35bISODocumentParameterGenerator, c36VariationOrExtensionOfSupervisionOrdersParameterGenerator,
            c39ChildAssessmentOrderParameterGenerator, c43ChildArrangementOrderDocumentParameterGenerator,
            c43aSGODocumentParameterGenerator, c45aParentalResponsibilityOrderDocumentParameterGenerator,
            c34BAuthorityToRefuseContactOrderParameterGenerator, c44aLeaveToChangeTheSurnameOrderParameterGenerator,
            c34AContactWithAChildInCareOrderDocumentParameterGenerator,
            c63aDeclarationOfParentageDocumentParameterGenerator,
            c42FamilyAssistanceOrderDocumentParameterGenerator, nonMolestationOrderDocumentParameterGenerator
        );
        collectors = List.of(c23EPOAdditionalDocumentsCollector);

        typeToGenerator = new HashMap<>() {
            {
                put(A70_PLACEMENT_ORDER, a70PlacementOrderDocumentParameterGenerator);
                put(C21_BLANK_ORDER, c21BlankOrderDocumentParameterGenerator);
                put(A81_PLACEMENT_BLANK_ORDER, a81PlacementBlankOrderDocumentParameterGenerator);
                put(C23_EMERGENCY_PROTECTION_ORDER, c23EPODocumentParameterGenerator);
                put(C26_SECURE_ACCOMMODATION_ORDER, c26SecureAccommodationOrderDocumentParameterGenerator);
                put(C29_RECOVERY_OF_A_CHILD, c29RecoveryOfAChildDocumentParameterGenerator);
                put(C32A_CARE_ORDER, c32CareOrderDocumentParameterGenerator);
                put(C32B_DISCHARGE_OF_CARE_ORDER, c32bDischargeOfCareOrderDocumentParameterGenerator);
                put(C33_INTERIM_CARE_ORDER, c33InterimCareOrderDocumentParameterGenerator);
                put(C35A_SUPERVISION_ORDER, c35aSupervisionOrderDocumentParameterGenerator);
                put(C35B_INTERIM_SUPERVISION_ORDER, c35bISODocumentParameterGenerator);
                put(C39_CHILD_ASSESSMENT_ORDER, c39ChildAssessmentOrderParameterGenerator);
                put(C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS,
                    c36VariationOrExtensionOfSupervisionOrdersParameterGenerator);
                put(C37_EDUCATION_SUPERVISION_ORDER_DIGITAL, c37EducationSupervisionOrderParameterGenerator);
                put(C43A_SPECIAL_GUARDIANSHIP_ORDER, c43aSGODocumentParameterGenerator);
                put(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                    c43ChildArrangementOrderDocumentParameterGenerator);
                put(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, c47AAppointmentOfAChildrensGuardianParameterGenerator);
                put(C45A_PARENTAL_RESPONSIBILITY_ORDER, c45aParentalResponsibilityOrderDocumentParameterGenerator);
                put(C34B_AUTHORITY_TO_REFUSE_CONTACT, c34BAuthorityToRefuseContactOrderParameterGenerator);
                put(C44A_LEAVE_TO_CHANGE_A_SURNAME, c44aLeaveToChangeTheSurnameOrderParameterGenerator);
                put(C34A_CONTACT_WITH_A_CHILD_IN_CARE, c34AContactWithAChildInCareOrderDocumentParameterGenerator);
                put(C63A_DECLARATION_OF_PARENTAGE, c63aDeclarationOfParentageDocumentParameterGenerator);
                put(C42_FAMILY_ASSISTANCE_ORDER, c42FamilyAssistanceOrderDocumentParameterGenerator);
                put(FL404A_NON_MOLESTATION_ORDER, nonMolestationOrderDocumentParameterGenerator);
            }
        };

        typeToAdditionalDocsCollector = Map.of(
            C23_EMERGENCY_PROTECTION_ORDER, c23EPOAdditionalDocumentsCollector
        );

        generators.forEach(generator -> when(generator.accept()).thenCallRealMethod());
        collectors.forEach(collector -> when(collector.accept()).thenCallRealMethod());
    }

    @Test
    void typeToGenerator() {
        assertThat(underTest.getTypeToGenerator()).isEqualTo(typeToGenerator);
    }

    @Test
    void typeToGeneratorCached() {
        underTest.getTypeToGenerator();
        assertThat(underTest.getTypeToGenerator()).isEqualTo(typeToGenerator);

        generators.forEach(generator -> verify(generator).accept());
    }

    @Test
    void typeToAdditionalDocsCollector() {
        assertThat(underTest.getTypeToAdditionalDocumentsCollector()).isEqualTo(typeToAdditionalDocsCollector);
    }

    @Test
    void typeToAdditionalDocsCollectorCached() {
        underTest.getTypeToGenerator();
        assertThat(underTest.getTypeToAdditionalDocumentsCollector()).isEqualTo(typeToAdditionalDocsCollector);

        collectors.forEach(collector -> verify(collector).accept());
    }

    @Test
    void shouldReturnAppropriateNotificationDocumentGenerators() {
        Map<Order, DocmosisParameterGenerator> expectedNotificationDocumentPerType = Map.of(
            A70_PLACEMENT_ORDER, a206PlacementOrderNotificationParameterGenerator
        );

        //Check expected orders match expected generators
        expectedNotificationDocumentPerType.forEach((order, generator) ->
            assertThat(underTest.getNotificationDocumentParameterGeneratorByOrderType(order))
                .as("Order %s", order)
                .isPresent()
                .hasValue(generator)
        );

        //Check remaining orders have no generator
        Set<Order> ordersWithNotificationDocument = expectedNotificationDocumentPerType.keySet();
        Arrays.stream(Order.values())
            .filter(not(ordersWithNotificationDocument::contains))
            .forEach(order -> assertThat(underTest.getNotificationDocumentParameterGeneratorByOrderType(order))
                .as("Order %s", order)
                .isEmpty()
            );
    }

}
