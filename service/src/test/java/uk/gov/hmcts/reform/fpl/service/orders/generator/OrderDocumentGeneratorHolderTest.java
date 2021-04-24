package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDocumentGeneratorHolderTest {

    private List<DocmosisParameterGenerator> generators;

    @Mock
    private C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;

    @Mock
    private C21BlankOrderDocumentParameterGenerator c21BlankOrderDocumentParameterGenerator;

    @Mock
    private C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;

    @InjectMocks
    private OrderDocumentGeneratorHolder underTest;

    @BeforeEach
    void setUp() {
        when(c32CareOrderDocumentParameterGenerator.accept()).thenCallRealMethod();
        when(c23EPODocumentParameterGenerator.accept()).thenCallRealMethod();
        generators = List.of(c32CareOrderDocumentParameterGenerator, c21BlankOrderDocumentParameterGenerator);

        generators.forEach(generator -> when(generator.accept()).thenCallRealMethod());
    }

    @Test
    void blockToValidator() {
        assertThat(underTest.getTypeToGenerator()).isEqualTo(Map.of(
            C21_BLANK_ORDER, c21BlankOrderDocumentParameterGenerator,
            C32_CARE_ORDER, c32CareOrderDocumentParameterGenerator,
            C23_EMERGENCY_PROTECTION_ORDER, c23EPODocumentParameterGenerator
        ));
    }

    @Test
    void blockToValidatorCached() {
        underTest.getTypeToGenerator();
        assertThat(underTest.getTypeToGenerator()).isEqualTo(Map.of(
            C21_BLANK_ORDER, c21BlankOrderDocumentParameterGenerator,
            C32_CARE_ORDER, c32CareOrderDocumentParameterGenerator,
            C23_EMERGENCY_PROTECTION_ORDER, c23EPODocumentParameterGenerator
        ));

        generators.forEach(generator -> verify(generator).accept());
    }
}
