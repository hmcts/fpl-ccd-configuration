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
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
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
        List<Element<GeneratedOrder>> generatedOrders = List.of(
            element(buildOrder("order 1", "12 March 1234", "Blank order (C21)")),
            element(buildOrder("order 2", "28 July 2020", "Blank order (C21)")),
            element(buildOrder("order 3", "29 August 2021", "Interim supervision order")),
            element(buildOrder("order 4", "12 August 2022", "Interim care order")),
            element(buildOrder("order 5", "12 September 2018", "Another Order"))
        );

        List<Element<CaseManagementOrder>> caseManagementOrders = List.of(
            element(CaseManagementOrder.builder()
                .status(APPROVED)
                .dateIssued(dateNow())
                .build()),
            element(CaseManagementOrder.builder()
                .status(DRAFT)
                .dateIssued(dateNow())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .orderCollection(generatedOrders)
            .sealedCMOs(caseManagementOrders)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("removableOrderList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(generatedOrders.get(0).getId(), "order 1 - 12 March 1234"),
                buildListElement(generatedOrders.get(1).getId(), "order 2 - 28 July 2020"),
                buildListElement(generatedOrders.get(2).getId(), "order 3 - 29 August 2021"),
                buildListElement(generatedOrders.get(3).getId(), "order 4 - 12 August 2022"),
                buildListElement(caseManagementOrders.get(0).getId(), String.format("Case management order - %s",
                    formatLocalDateToString(dateNow(), "d MMMM yyyy")))
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

    private GeneratedOrder buildOrder(String title, String date, String type) {
        return GeneratedOrder.builder()
            .type(type)
            .title(title)
            .dateOfIssue(date)
            .build();
    }
}
