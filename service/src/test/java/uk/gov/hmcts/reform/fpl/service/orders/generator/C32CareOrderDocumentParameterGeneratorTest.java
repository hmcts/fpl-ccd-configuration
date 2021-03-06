package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32CareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C32CareOrderDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final Child CHILD = mock(Child.class);
    private static final GeneratedOrderType TYPE = GeneratedOrderType.CARE_ORDER;
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
            .build())
        .build();
    private static final String ORDER_HEADER = "Care order restrictions";
    private static final String ORDER_MESSAGE = "Care order message";

    @Mock
    private ChildrenSmartSelector childrenSmartSelector;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @InjectMocks
    private C32CareOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C32A_CARE_ORDER);
    }

    @Test
    void singularOrderDetails() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getCareOrderRestrictions(CASE_DATA)).thenReturn(ORDER_MESSAGE);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is ordered that the child is placed in the care of " + LA_NAME + ".")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void pluralOrderDetails() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);

        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getCareOrderRestrictions(CASE_DATA)).thenReturn(ORDER_MESSAGE);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is ordered that the children are placed in the care of " + LA_NAME + ".")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    private C32CareOrderDocmosisParameters.C32CareOrderDocmosisParametersBuilder<?,?> expectedCommonParameters() {
        return C32CareOrderDocmosisParameters.builder()
            .orderTitle(Order.C32A_CARE_ORDER.getTitle())
            .orderHeader(ORDER_HEADER)
            .orderMessage(ORDER_MESSAGE)
            .orderType(TYPE)
            .furtherDirections(FURTHER_DIRECTIONS)
            .localAuthorityName(LA_NAME);
    }
}
