package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43ChildArrangementOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class C43ChildArrangementOrderDocumentParameterGeneratorTest {
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String ORDER_HEADER = "Warning \n";
    private static final String RECITALS_AND_PREAMBLES = "Recitals and Preambles";
    private static final String DIRECTIONS = "Directions";
    private static final String FURTHER_DIRECTIONS = "Further directions";

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C43ChildArrangementOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C43_CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER);
    }

    @Test
    void shouldShowWarning() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            C43OrderType.SPECIFIC_ISSUE_ORDER, C43OrderType.PROHIBITED_STEPS_ORDER);

        String orderTitle = "Child arrangements, Specific issue and Prohibited steps order";

        CaseData caseData = buildCaseData(c43OrderTypes);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters(orderTitle)
            .orderHeader(ORDER_HEADER)
            .orderMessage(C43ChildArrangementOrderDocumentParameterGenerator.WARNING)
            .build());
    }

    @Test
    void shouldNotShowWarning() {
        List<C43OrderType> c43OrderTypes = List.of(
            C43OrderType.SPECIFIC_ISSUE_ORDER, C43OrderType.PROHIBITED_STEPS_ORDER);

        String orderTitle = "Specific issue and Prohibited steps order";

        CaseData caseData = buildCaseData(c43OrderTypes);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters(orderTitle).build());
    }

    @Test
    void expectedOrderTitleWhenOneOrderSelected() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.SPECIFIC_ISSUE_ORDER);

        String orderTitle = "Specific issue order";

        CaseData caseData = buildCaseData(c43OrderTypes);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters(orderTitle).build());
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    private CaseData buildCaseData(List<C43OrderType> c43OrderTypes) {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C43_CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER)
                .manageOrdersC43Orders(c43OrderTypes)
                .manageOrdersRecitalsAndPreambles(RECITALS_AND_PREAMBLES)
                .manageOrdersC43Directions(DIRECTIONS)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .build();
    }

    private C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters(String orderTitle) {
        String orderDetails = String.format("The Court orders\n\n%s", RECITALS_AND_PREAMBLES);
        String directions = String.format("%s\n\n%s\n\n%s", DIRECTIONS, FURTHER_DIRECTIONS,
            C43ChildArrangementOrderDocumentParameterGenerator.WHERE);

        return C43ChildArrangementOrderDocmosisParameters.builder()
            .orderTitle(orderTitle)
            .orderDetails(orderDetails)
            .furtherDirections(directions)
            .localAuthorityName(LA_NAME)
            .noticeHeader("Notice")
            .noticeMessage(C43ChildArrangementOrderDocumentParameterGenerator.NOTICE);
    }
}
