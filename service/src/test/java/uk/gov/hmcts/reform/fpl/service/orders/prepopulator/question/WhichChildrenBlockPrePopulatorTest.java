package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WhichChildrenBlockPrePopulatorTest {

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
        final Element<Child> child1 = Element.<Child>builder()
            .id(UUID.randomUUID())
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("first1")
                    .lastName("last1")
                    .gender(ChildGender.BOY)
                    .build())
                .build())
            .build();
        final Element<Child> child2 = Element.<Child>builder()
            .id(UUID.randomUUID()).value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("first2")
                    .lastName("last2")
                    .gender(ChildGender.OTHER)
                    .build())
                .build())
            .build();

        final List<Element<Child>> childrenList = List.of(child1, child2);

        final DynamicMultiSelectList childSelectorForManageOrders = DynamicMultiSelectList.builder().listItems(
            List.of(
                DynamicMultiSelectListElement.builder().code(child1.getId().toString())
                    .label("first1 last1 (Child 1)").build(),
                DynamicMultiSelectListElement.builder().code(child2.getId().toString())
                    .label("first2 last2 (Child 2)").build()
            )
        ).build();

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .build();

        when(childrenService.getChildrenLabel(childrenList, false)).thenReturn(CHILDREN_LABEL);
        when(childrenService.getChildrenMultiSelectList(caseData)).thenReturn(childSelectorForManageOrders);
        when(childrenService.getChildrenLabelFromMultiSelectList(childSelectorForManageOrders))
            .thenReturn("first1 last1 (Child 1)\nfirst2 last2 (Child 2)");

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                "childSelectorForManageOrders", childSelectorForManageOrders,
                "children_label", "first1 last1 (Child 1)\nfirst2 last2 (Child 2)"
            )
        );
    }
}
