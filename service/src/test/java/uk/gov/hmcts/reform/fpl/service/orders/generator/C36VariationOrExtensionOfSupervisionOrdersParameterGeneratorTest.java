package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C36OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.C36OrderType.EXTENSION_OF_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C36OrderType.VARIATION_OF_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS;

class C36VariationOrExtensionOfSupervisionOrdersParameterGeneratorTest {

    private static final String ORDER_DIRECTION = "Test Order Direction";
    private static final LocalDate APPROVAL_DATE = LocalDate.of(2022, 1, 15);
    private static final LocalDate END_DATE = LocalDate.of(2022, 12, 15);

    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);

    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator orderMessageGenerator = new OrderMessageGenerator(manageOrderDocumentService);

    private C36VariationOrExtensionOfSupervisionOrdersParameterGenerator underTest =
        new C36VariationOrExtensionOfSupervisionOrdersParameterGenerator(
            orderMessageGenerator
        );

    @BeforeEach
    void setUp() {
        Map<String, String> context = new HashMap<>();
        context.put("childOrChildren", "child");
        context.put("childIsOrAre", "is");
        context.put("localAuthorityName", LOCAL_AUTHORITY_1_NAME);
        context.put("courtName", LOCAL_AUTHORITY_1_COURT_NAME);

        when(manageOrderDocumentService.commonContextElements(any())).thenReturn(context);
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_1_CODE)).thenReturn(LOCAL_AUTHORITY_1_NAME);
    }

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void generateOrderWithVariationOrder() {
        CaseData caseData = buildCaseDataWithSpecifiedC36OrderType(VARIATION_OF_SUPERVISION_ORDER);
        DocmosisParameters docParam = underTest.generate(caseData);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        DocmosisParameters expectedParam = buildExpectedParameters(VARIATION_OF_SUPERVISION_ORDER, caseData, eventData);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithExtensionOrder() {
        CaseData caseData = buildCaseDataWithSpecifiedC36OrderType(EXTENSION_OF_SUPERVISION_ORDER);
        DocmosisParameters docParam = underTest.generate(caseData);
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        DocmosisParameters expectedParam = buildExpectedParameters(EXTENSION_OF_SUPERVISION_ORDER, caseData, eventData);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    private CaseData buildCaseDataWithSpecifiedC36OrderType(C36OrderType orderType) {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .court(COURT_1)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS)
                .manageOrdersSupervisionOrderType(orderType)
                .manageOrdersSupervisionOrderCourtDirection(ORDER_DIRECTION)
                .manageOrdersSupervisionOrderApprovalDate(APPROVAL_DATE)
                .manageOrdersSupervisionOrderEndDate(END_DATE)
                .manageOrdersIsByConsent(YES.getValue())
                .build())
            .build();
    }

    private DocmosisParameters buildExpectedParameters(
        C36OrderType orderType,
        CaseData caseData,
        ManageOrdersEventData eventData
    ) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (orderType) {
            case VARIATION_OF_SUPERVISION_ORDER:
                stringBuilder.append("The Court varies the Supervision Order for the Care Order ");
                break;
            case EXTENSION_OF_SUPERVISION_ORDER:
                stringBuilder.append("The Court extends the Supervision Order for the Care Order ");
                break;
        }

        stringBuilder
            .append("made by this Court, " + LOCAL_AUTHORITY_1_COURT_NAME + " on ")
            .append(APPROVAL_DATE)
            .append(".\n\n");

        stringBuilder
            .append("The Court orders " + LOCAL_AUTHORITY_1_NAME + " to supervise the child")
            .append(".\n\n");

        stringBuilder
            .append("The Court directs ")
            .append(ORDER_DIRECTION)
            .append(".\n\n");

        stringBuilder
            .append("This order ends on ")
            .append(END_DATE)
            .append(".\n\n");

        String orderDetails = orderMessageGenerator.formatOrderMessage(caseData, stringBuilder.toString());

        return C36VariationOrExtensionOfSupervisionOrdersDocmosisParameters.builder()
            .orderTitle(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getTitle())
            .childrenAct(Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS.getChildrenAct())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(orderDetails)
            .build();
    }
}
