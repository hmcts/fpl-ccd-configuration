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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32A_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C33_INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35B_INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDocumentGeneratorHolderTest {

    private List<DocmosisParameterGenerator> generators;
    private List<AdditionalDocumentsCollector> collectors;
    private Map<Order, DocmosisParameterGenerator> typeToGenerator;
    private Map<Order, AdditionalDocumentsCollector> typeToAdditionalDocsCollector;

    // Parameter Generators
    @Mock
    private C21BlankOrderDocumentParameterGenerator c21BlankOrderDocumentParameterGenerator;
    @Mock
    private C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;
    @Mock
    private C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;
    @Mock
    private C32bDischargeOfCareOrderDocumentParameterGenerator c32bDischargeOfCareOrderDocumentParameterGenerator;
    @Mock
    private C33InterimCareOrderDocumentParameterGenerator c33InterimCareOrderDocumentParameterGenerator;
    @Mock
    private C35aSupervisionOrderDocumentParameterGenerator c35aSupervisionOrderDocumentParameterGenerator;
    @Mock
    private C35bISODocumentParameterGenerator c35bISODocumentParameterGenerator;
    @Mock
    private C43aSpecialGuardianshipOrderDocumentParameterGenerator c43aSGODocumentParameterGenerator;
    @Mock
    private C47AAppointmentOfAChildrensGuardianParameterGenerator c47AAppointmentOfAChildrensGuardianParameterGenerator;

    // Additional Document Collectors
    @Mock
    private C23EPOAdditionalDocumentsCollector c23EPOAdditionalDocumentsCollector;

    @InjectMocks
    private OrderDocumentGeneratorHolder underTest;

    @BeforeEach
    void setUp() {
        generators = List.of(
            c21BlankOrderDocumentParameterGenerator, c23EPODocumentParameterGenerator,
            c32CareOrderDocumentParameterGenerator, c32bDischargeOfCareOrderDocumentParameterGenerator,
            c33InterimCareOrderDocumentParameterGenerator, c35aSupervisionOrderDocumentParameterGenerator,
            c35bISODocumentParameterGenerator, c43aSGODocumentParameterGenerator,
            c47AAppointmentOfAChildrensGuardianParameterGenerator
        );
        collectors = List.of(c23EPOAdditionalDocumentsCollector);

        typeToGenerator = Map.of(
            C21_BLANK_ORDER, c21BlankOrderDocumentParameterGenerator,
            C23_EMERGENCY_PROTECTION_ORDER, c23EPODocumentParameterGenerator,
            C32A_CARE_ORDER, c32CareOrderDocumentParameterGenerator,
            C32B_DISCHARGE_OF_CARE_ORDER, c32bDischargeOfCareOrderDocumentParameterGenerator,
            C33_INTERIM_CARE_ORDER, c33InterimCareOrderDocumentParameterGenerator,
            C35A_SUPERVISION_ORDER, c35aSupervisionOrderDocumentParameterGenerator,
            C35B_INTERIM_SUPERVISION_ORDER, c35bISODocumentParameterGenerator,
            C43A_SPECIAL_GUARDIANSHIP_ORDER, c43aSGODocumentParameterGenerator,
            C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, c47AAppointmentOfAChildrensGuardianParameterGenerator
        );

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
}
