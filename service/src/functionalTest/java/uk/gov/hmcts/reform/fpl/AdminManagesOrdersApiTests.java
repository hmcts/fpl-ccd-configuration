package uk.gov.hmcts.reform.fpl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import java.time.LocalDate;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

public class AdminManagesOrdersApiTests extends AbstractApiTest {

    private final LocalDate approvalDate = now();
    private CaseData startingCaseData;

    @Autowired
    private DocumentService documentService;

    @Before
    public void setUp() {
        startingCaseData = createCase("admin-manage-orders/c32_order_case.json", LA_SWANSEA_USER_1);
    }

    @Test
    public void c32OrderTest() {
        CaseData caseData = callAboutToSubmit(startingCaseData);
        @NotNull @Valid GeneratedOrder generatedOrder = caseData.getOrderCollection().get(0).getValue();
        assertThat(generatedOrder.getOrderType().equals("C32A_CARE_ORDER"));
    }


    private CaseData callAboutToSubmit(CaseData caseData) {
        CaseData updatedCase = caseData.toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C32A_CARE_ORDER)
                .manageOrdersTitle("Order title")
                .manageOrdersDirections("Order details")
                .manageOrdersApprovalDate(approvalDate)
                .build())
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-orders/about-to-submit");

        GeneratedOrder order = unwrapElements(response.getCaseData().getOrderCollection()).get(0);

        String actualOrderContent = documentService.getPdfContent(order.getDocument(), COURT_ADMIN);

        String expectedOrderContent = readString("admin-manage-orders/c32order.txt",
            Map.of("id", formatCCDCaseNumber(caseData.getId()),
                "issueDate", formatLocalDateToString(approvalDate, DATE)
            ));

        assertThat(actualOrderContent).isEqualToNormalizingWhitespace(expectedOrderContent);

        return response.getCaseData();
    }
}
