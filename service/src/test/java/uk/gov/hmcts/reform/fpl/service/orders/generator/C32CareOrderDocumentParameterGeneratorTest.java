package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32CareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({SpringExtension.class})
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

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C32CareOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C32_CARE_ORDER);
    }

    @Test
    void singularOrderDetails() {
        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);

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

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails("It is ordered that the children are placed in the care of " + LA_NAME + ".")
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER);
    }

    private C32CareOrderDocmosisParameters.C32CareOrderDocmosisParametersBuilder<?,?> expectedCommonParameters() {
        return C32CareOrderDocmosisParameters.builder()
            .orderTitle(Order.C32_CARE_ORDER.getTitle())
            .orderType(TYPE)
            .furtherDirections(FURTHER_DIRECTIONS)
            .localAuthorityName(LA_NAME);
    }
}
