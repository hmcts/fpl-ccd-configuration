package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.AmendedOrderListManager;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersControllerAboutToStartTest extends AbstractCallbackTest {
    private static final UUID UHO_ID = UUID.fromString("5d05d011-5d01-5d01-5d01-5d05d05d05d0");

    @Mock
    private UrgentHearingOrder uho;

    ManageOrdersControllerAboutToStartTest() {
        super("manage-orders");
    }

    @Test
    void shouldNotPopulateManageOrdersAmendmentListWhenStateIsClosed() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .build();

        CaseData returnedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(returnedCaseData.getManageOrdersEventData().getManageOrdersAmendmentList()).isNull();
    }

    @Test
    void shouldPopulateManageOrdersAmendmentListWhenStateIsNotClosed() {
        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .urgentHearingOrder(uho)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("manageOrdersAmendmentList"), DynamicList.class
        );

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(UHO_ID, "Urgent hearing order")))
            .build();

        assertThat(builtDynamicList).isEqualTo(expectedList);
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

}
