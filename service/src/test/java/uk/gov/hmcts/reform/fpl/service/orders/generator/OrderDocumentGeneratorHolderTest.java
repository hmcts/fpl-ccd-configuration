package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDocumentGeneratorHolderTest {

    @Mock
    private C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;

    @InjectMocks
    private OrderDocumentGeneratorHolder underTest;


    @BeforeEach
    void setUp() {
        when(c32CareOrderDocumentParameterGenerator.accept()).thenCallRealMethod();
    }

    @Test
    void blockToValidator() {
        assertThat(underTest.getTypeToGenerator()).isEqualTo(Map.of(
            C32_CARE_ORDER, c32CareOrderDocumentParameterGenerator
        ));
    }

    @Test
    void blockToValidatorCached() {
        underTest.getTypeToGenerator();
        assertThat(underTest.getTypeToGenerator()).isEqualTo(Map.of(
            C32_CARE_ORDER, c32CareOrderDocumentParameterGenerator
        ));

        verify(c32CareOrderDocumentParameterGenerator, times(1)).accept();
    }

}
