package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.SECTION_4;

@ExtendWith(SpringExtension.class)
class OrderSectionAndQuestionsPrePopulatorHolderTest {

    @MockBean
    private ChildrenService childrenService;
    @MockBean
    private OrderDocumentGenerator orderDocumentGenerator;

    @SpyBean
    private WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;

    @SpyBean
    private ApproverBlockPrePopulator approverBlockPrePopulator;

    @SpyBean
    private DraftOrderPreviewSectionPrePopulator draftOrderPreviewSectionPrePopulator;

    private OrderSectionAndQuestionsPrePopulatorHolder underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderSectionAndQuestionsPrePopulatorHolder(
            whichChildrenBlockPrePopulator,
            approverBlockPrePopulator,
            draftOrderPreviewSectionPrePopulator
        );
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
                SECTION_4, draftOrderPreviewSectionPrePopulator
            )
        );

    }

    @Test
    void sectionBlockToPopulatorCached() {

        underTest.sectionBlockToPopulator();

        assertThat(underTest.sectionBlockToPopulator()).isEqualTo(
            Map.of(
                SECTION_4, draftOrderPreviewSectionPrePopulator
            )
        );

        verify(draftOrderPreviewSectionPrePopulator,times(1)).accept();

    }
}
