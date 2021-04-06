package uk.gov.hmcts.reform.fpl.service.orders;

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
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderValidatorHolderTest {

    @Mock
    private ApprovalDateValidator approvalDateValidator;

    @InjectMocks
    private OrderValidatorHolder underTest;

    @BeforeEach
    void setUp() {
        when(approvalDateValidator.accept()).thenCallRealMethod();
    }

    @Test
    void blockToValidator() {
        assertThat(underTest.blockToValidator()).isEqualTo(Map.of(
            APPROVAL_DATE, approvalDateValidator
        ));
    }

    @Test
    void blockToValidatorCached() {
        underTest.blockToValidator();
        assertThat(underTest.blockToValidator()).isEqualTo(Map.of(
            APPROVAL_DATE, approvalDateValidator
        ));

        verify(approvalDateValidator, times(1)).accept();
    }
}
