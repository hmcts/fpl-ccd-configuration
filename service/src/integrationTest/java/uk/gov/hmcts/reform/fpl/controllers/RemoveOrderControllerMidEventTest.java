package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveOrderControllerMidEventTest extends AbstractControllerTest {

    private Element<GeneratedOrder> selectedOrder;

    RemoveOrderControllerMidEventTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    @Test
    void shouldExtractSelectedOrderFields() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(selectedOrder.getValue().getDocument(),
                new TypeReference<Map<String, Object>>() {}),
            "orderTitleToBeRemoved", selectedOrder.getValue().getTitle(),
            "orderIssuedDateToBeRemoved", selectedOrder.getValue().getDateOfIssue(),
            "orderDateToBeRemoved", selectedOrder.getValue().getDate()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldRegenerateDynamicList() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
        DynamicList removableOrderList = mapper.convertValue(responseData.getRemovableOrderList(), DynamicList.class);

        assertThat(removableOrderList).isEqualTo(buildRemovableOrderList(selectedOrder.getId()));
    }

    private CaseData buildCaseData(Element<GeneratedOrder> order) {
        return CaseData.builder()
            .orderCollection(List.of(order))
            .removableOrderList(buildRemovableOrderList(order.getId()))
            .build();
    }

    private DynamicList buildRemovableOrderList(UUID id) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build()))
            .build();
    }

    private GeneratedOrder buildOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .date("11:23am, 12 March 1234")
            .document(DocumentReference.builder().build())
            .build();
    }
}
