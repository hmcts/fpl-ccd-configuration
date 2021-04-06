package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class WhichChildrenBlockPrePopulatorTest {

    private static final List<Integer> HIDDEN_LIST = List.of(1);
    private static final String CHILDREN_LABEL = "children label";
    private final ChildrenService childrenService = mock(ChildrenService.class);

    private final WhichChildrenBlockPrePopulator underTest = new WhichChildrenBlockPrePopulator(
        childrenService
    );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.WHICH_CHILDREN);
    }

    @Test
    void prePopulate() {
        List<Element<Child>> children = wrapElements(mock(Child.class), mock(Child.class));
        CaseData caseData = CaseData.builder()
            .children1(children)
            .build();

        when(childrenService.getIndexesOfChildrenWithFinalOrderIssued(caseData)).thenReturn(HIDDEN_LIST);
        when(childrenService.getChildrenLabel(children, true)).thenReturn(CHILDREN_LABEL);

        assertThat(underTest.prePopulate(caseData, null)).isEqualTo(
            Map.of(
                "childSelector", Selector.builder()
                    .hidden(HIDDEN_LIST)
                    .build().setNumberOfOptions(2),
                "children_label", CHILDREN_LABEL
            )
        );
    }
}
