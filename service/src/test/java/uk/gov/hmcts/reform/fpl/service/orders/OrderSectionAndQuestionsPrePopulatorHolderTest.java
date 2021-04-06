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
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.REVIEW;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderSectionAndQuestionsPrePopulatorHolderTest {

    @Mock
    private WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;

    @Mock
    private ApproverBlockPrePopulator approverBlockPrePopulator;

    @Mock
    private DraftOrderPreviewSectionPrePopulator draftOrderPreviewSectionPrePopulator;

    @InjectMocks
    private OrderSectionAndQuestionsPrePopulatorHolder underTest;

    @BeforeEach
    void setUp() {
        when(whichChildrenBlockPrePopulator.accept()).thenCallRealMethod();
        when(approverBlockPrePopulator.accept()).thenCallRealMethod();
        when(draftOrderPreviewSectionPrePopulator.accept()).thenCallRealMethod();
    }

    @Test
    void questionBlockToPopulator() {

        assertThat(underTest.questionBlockToPopulator()).isEqualTo(
            Map.of(
                APPROVER, approverBlockPrePopulator,
                WHICH_CHILDREN, whichChildrenBlockPrePopulator
            )
        );
    }

    @Test
    void questionBlockToPopulatorCached() {
        underTest.questionBlockToPopulator();

        assertThat(underTest.questionBlockToPopulator()).isEqualTo(
            Map.of(
                APPROVER, approverBlockPrePopulator,
                WHICH_CHILDREN, whichChildrenBlockPrePopulator
            )
        );

        verify(approverBlockPrePopulator,times(1)).accept();
        verify(whichChildrenBlockPrePopulator,times(1)).accept();
    }

    @Test
    void sectionBlockToPopulator() {
        assertThat(underTest.sectionBlockToPopulator()).isEqualTo(
            Map.of(
                REVIEW, draftOrderPreviewSectionPrePopulator
            )
        );
    }

    @Test
    void sectionBlockToPopulatorCached() {
        underTest.sectionBlockToPopulator();

        assertThat(underTest.sectionBlockToPopulator()).isEqualTo(
            Map.of(
                REVIEW, draftOrderPreviewSectionPrePopulator
            )
        );

        verify(draftOrderPreviewSectionPrePopulator,times(1)).accept();
    }
}
