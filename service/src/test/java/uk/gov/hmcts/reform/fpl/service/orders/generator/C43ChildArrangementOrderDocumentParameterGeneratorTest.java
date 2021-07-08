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
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class C43ChildArrangementOrderDocumentParameterGeneratorTest {
    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final String CONSENT = "By consent";
    private static final String ORDER_HEADER = "Warning \n";
    private static final String RECITALS_AND_PREAMBLES = "Recitals and Preambles";
    private static final String DIRECTIONS = "Directions";
    private static final String FURTHER_DIRECTIONS = "Further directions";
    private static final String ORDER_TITLE = "Title";

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @Mock
    private C43ChildArrangementOrderTitleGenerator c43ChildArrangementOrderTitleGenerator;

    @InjectMocks
    private C43ChildArrangementOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER);
    }

    @Test
    void shouldShowWarningWhenChildArrangementOrder() {
        List<C43OrderType> c43OrderTypes = List.of(C43OrderType.CHILD_ARRANGEMENT_ORDER,
            C43OrderType.SPECIFIC_ISSUE_ORDER, C43OrderType.PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters()
            .orderHeader(ORDER_HEADER)
            .orderMessage(C43ChildArrangementOrderDocumentParameterGenerator.WARNING_MESSAGE)
            .build());
    }

    @Test
    void shouldNotShowWarningWhenNoChildArrangementOrder() {
        List<C43OrderType> c43OrderTypes = List.of(
            C43OrderType.SPECIFIC_ISSUE_ORDER, C43OrderType.PROHIBITED_STEPS_ORDER);

        CaseData caseData = buildCaseData(c43OrderTypes);

        when(laNameLookup.getLocalAuthorityName(LA_CODE)).thenReturn(LA_NAME);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);
        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(any())).thenReturn(ORDER_TITLE);

        DocmosisParameters generatedParameters = underTest.generate(caseData);

        assertThat(generatedParameters).isEqualTo(expectedCommonParameters().build());
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    private CaseData buildCaseData(List<C43OrderType> c43OrderTypes) {
        return CaseData.builder()
            .caseLocalAuthority(LA_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER)
                .manageOrdersIsByConsent("No")
                .manageOrdersMultiSelectListForC43(c43OrderTypes)
                .manageOrdersRecitalsAndPreambles(RECITALS_AND_PREAMBLES)
                .manageOrdersDirections(DIRECTIONS)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .build();
    }

    private C43ChildArrangementOrderDocmosisParameters.C43ChildArrangementOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters() {
        String orderDetails = String.format("The Court orders\n\n%s", RECITALS_AND_PREAMBLES);
        String directions = String.format("%s\n\n%s\n\n%s", DIRECTIONS, FURTHER_DIRECTIONS,
            C43ChildArrangementOrderDocumentParameterGenerator.CONDITIONS_MESSAGE);

        return C43ChildArrangementOrderDocmosisParameters.builder()
            .orderTitle(ORDER_TITLE)
            .orderByConsent(CONSENT)
            .orderDetails(orderDetails)
            .furtherDirections(directions)
            .localAuthorityName(LA_NAME)
            .noticeHeader("Notice")
            .noticeMessage(C43ChildArrangementOrderDocumentParameterGenerator.NOTICE_MESSAGE);
    }
}
