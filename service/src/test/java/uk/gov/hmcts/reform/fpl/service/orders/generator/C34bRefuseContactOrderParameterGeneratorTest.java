package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.RespondentsRefusedFormatter;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C34BAuthorityToRefuseContactDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34B_AUTHORITY_TO_REFUSE_CONTACT;

@ExtendWith({MockitoExtension.class})
class C34bRefuseContactOrderParameterGeneratorTest {

    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    public static final String CONSENT = "By consent";

    @Mock
    private RespondentsRefusedFormatter respondentsRefusedFormatter;

    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @InjectMocks
    private C34BAuthorityToRefuseContactOrderParameterGenerator underTest;

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C34B_AUTHORITY_TO_REFUSE_CONTACT);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void generateDocumentForSingleChildWithOrderByConsent() {
        CaseData caseData = getCaseData(true, 1);

        when(respondentsRefusedFormatter.getRespondentsNamesForDocument(caseData)).thenReturn("Remmy Respondent");
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(orderMessageGenerator.formatOrderMessage(caseData, "The local authority is ${localAuthorityName}")).thenReturn("The local authority is a region");
        when(orderMessageGenerator.formatOrderMessage(caseData,"The Court orders that the local authority is authorised to refuse contact between the ${childOrChildren} and "
        )).thenReturn("The Court orders that the local authority is authorised to refuse contact between the child and ");

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
                .orderDetails(getOrderDetailForChildWithSingleRespondent())
                .orderMessage(getOrderMessageForLocalAuthority())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForMultipleChildrenWithOrderByConsent() {
        CaseData caseData = getCaseData(true, 1);

        when(respondentsRefusedFormatter.getRespondentsNamesForDocument(caseData)).thenReturn("Remmy Respondent");
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(orderMessageGenerator.formatOrderMessage(caseData, "The local authority is ${localAuthorityName}")).thenReturn("The local authority is a region");

        String testMessage = "The Court orders that the local authority is authorised to refuse contact between the children and "
            + respondentsRefusedFormatter.getRespondentsNamesForDocument(caseData);

        when(underTest.getRespondentsRefusedMessage(caseData)).thenReturn(testMessage);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderDetailForChildWithSingleRespondent())
            .orderMessage(getOrderMessageForLocalAuthority())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getOrderDetailForChildWithSingleRespondent() {
        return "The Court orders that the local authority is authorised to refuse contact between the child and Remmy Respondent";
    }

    private String getOrderMessageForLocalAuthority() {
        return "The local authority is a region";
    }

    private C34BAuthorityToRefuseContactDocmosisParameters.C34BAuthorityToRefuseContactDocmosisParametersBuilder<?, ?>
        expectedCommonParameters(Boolean isOrderByConsent) {
        String orderByConsentContent = getOrderByConsentContent(isOrderByConsent);

        return C34BAuthorityToRefuseContactDocmosisParameters.builder()
                .orderTitle(C34B_AUTHORITY_TO_REFUSE_CONTACT.getTitle())
                .childrenAct(C34B_AUTHORITY_TO_REFUSE_CONTACT.getChildrenAct())
                .orderByConsent(orderByConsentContent);
    }

    private String getOrderByConsentContent(Boolean isOrderByConsent) {
        String orderByConsentContent = "By consent";
        if (!isOrderByConsent) {
            orderByConsentContent = null;
        }
        return orderByConsentContent;
    }

    private CaseData getCaseData(boolean isOrderByConsent, int numOfRespondents) {

        return CaseData.builder()
            .respondentsRefusedSelector(Selector.builder().selected(List.of(numOfRespondents)).build())
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersIsByConsent(isOrderByConsent ? "Yes" : "No")
                .build())
            .build();
    }
}
