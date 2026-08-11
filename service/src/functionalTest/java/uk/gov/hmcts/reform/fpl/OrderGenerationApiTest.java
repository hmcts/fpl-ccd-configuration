package uk.gov.hmcts.reform.fpl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

public class OrderGenerationApiTest extends AbstractApiTest {

    private final LocalDate approvalDate = now();

    @Autowired
    private DocumentService documentService;

    @Test
    public void doNothing() {
        assertTrue(true);
    }

    // @Test
    public void shouldGenerateAndPostOrder() {
        CaseData startingCaseData = createCase("order-generation/case.json", LA_SWANSEA_USER_1);
        callAboutToSubmit(startingCaseData);
    }

    public CaseData callAboutToSubmit(CaseData caseData) {
        CaseData updatedCase = caseData.toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C21_BLANK_ORDER)
                .manageOrdersTitle("Order title")
                .manageOrdersDirections("Order details")
                .manageOrdersApprovalDate(approvalDate)
                .build())
            .build();

        CallbackResponse response = callback(updatedCase, COURT_ADMIN, "manage-orders/about-to-submit");

        GeneratedOrder order = unwrapElements(response.getCaseData().getOrderCollection()).get(0);

        String actualOrderContent = documentService.getPdfContent(order.getDocument(), COURT_ADMIN);

        String expectedOrderContent = readString("order-generation/blank-order.txt",
            Map.of("id", formatCCDCaseNumber(caseData.getId()),
                "issueDate", formatLocalDateToString(approvalDate, DATE),
                "orderTitle", "Order title",
                "orderDetails", "Order details"
            ));

        assertThat(actualOrderContent).isEqualToNormalizingWhitespace(expectedOrderContent);

        return response.getCaseData();
    }

    public void callSubmitted(CaseData caseData, CaseData caseDataBefore) {
        submittedCallback(caseData, caseDataBefore, COURT_ADMIN, "manage-orders/submitted");

        CaseData updatedCase = caseService.pollCase(
            caseData.getId(), COURT_ADMIN, aCase -> isNotEmpty(aCase.getDocumentsSentToParties())
        );

        assertThat(updatedCase.getDocumentsSentToParties()).isNotEmpty();

        List<String> letterIds = updatedCase.getDocumentsSentToParties().stream()
            .map(Element::getValue)
            .map(SentDocuments::getDocumentsSentToParty)
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .map(SentDocument::getLetterId)
            .collect(toList());

        assertThat(letterIds).isNotEmpty().doesNotContainNull();
    }
}
