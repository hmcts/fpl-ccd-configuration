package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveOrderControllerMidEventTest extends AbstractControllerTest {
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private Element<GeneratedOrder> selectedOrder;

    RemoveOrderControllerMidEventTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    @Test
    void shouldExtractSelectedGeneratedOrderFields() {
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
    void shouldExtractSelectedCaseManagementOrderFields() {
        UUID removedOrderId = UUID.randomUUID();
        DocumentReference documentReference = DocumentReference.builder().build();

        HearingBooking hearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now())
            .caseManagementOrderId(removedOrderId)
            .build();

        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .order(documentReference)
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(removedOrderId)
                .label("Case management order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .sealedCMOs(List.of(element(removedOrderId, caseManagementOrder)))
            .hearingDetails(List.of(element(hearingBooking)))
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(caseManagementOrder.getOrder(),
                new TypeReference<Map<String, Object>>() {}),
            "orderTitleToBeRemoved", "Case management order",
            "hearingToUnlink", hearingBooking.toLabel()
        );

        assertThat(responseData).containsAllEntriesOf(extractedFields);
    }

    @Test
    void shouldExtractSelectedStandardDirectionOrderFields() {
        DocumentReference documentReference = DocumentReference.builder().build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .orderDoc(documentReference)
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(SDO_ID)
                .label("Gatekeeping order - 12 March 1234")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(standardDirectionOrder)
            .removableOrderList(dynamicList)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        Map<String, Object> responseData = response.getData();

        Map<String, Object> extractedFields = Map.of(
            "orderToBeRemoved", mapper.convertValue(standardDirectionOrder.getOrderDoc(),
                new TypeReference<Map<String, Object>>() {}),
            "orderTitleToBeRemoved", "Gatekeeping order",
            "showRemoveSDOWarningFlag", YES.getValue()
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
