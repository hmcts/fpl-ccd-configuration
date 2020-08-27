package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveOrderControllerAboutToStartTest extends AbstractControllerTest {
    RemoveOrderControllerAboutToStartTest() {
        super("remove-order");
    }

    @Test
    void shouldAddRemovableOrderListToCaseData() {
        List<Element<GeneratedOrder>> orders = List.of(
            element(buildOrder("order 1", "12 March 1234")),
            element(buildOrder("order 2", "28 July 2020"))
        );

        CaseData caseData = CaseData.builder()
            .orderCollection(orders)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
        DynamicList builtDynamicList = mapper.convertValue(responseData.getRemovableOrderList(), DynamicList.class);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(orders.get(0).getId(), "order 1 - 12 March 1234"),
                buildListElement(orders.get(1).getId(), "order 2 - 28 July 2020")
            ))
            .build();

        assertThat(builtDynamicList).isEqualTo(expectedList);
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder(String title, String date) {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title(title)
            .dateOfIssue(date)
            .build();
    }
}
