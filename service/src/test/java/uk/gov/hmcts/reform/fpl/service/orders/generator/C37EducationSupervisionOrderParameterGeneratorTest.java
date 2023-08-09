package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrderEndDateOption;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C37EducationSupervisionOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrderEndDateOption.TWELVE_MONTHS_FROM_DATE_OF_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrderEndDateOption.UNTIL_END_OF_COMPULSORY_EDUCATION_AGE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL;

class C37EducationSupervisionOrderParameterGeneratorTest {

    private static final String LA_NAME = "Testing LA Name";
    private static final String LEA_NAME = "Testing LEA";

    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator orderMessageGenerator = new OrderMessageGenerator(manageOrderDocumentService);

    private C37EducationSupervisionOrderParameterGenerator underTest =
        new C37EducationSupervisionOrderParameterGenerator(
            orderMessageGenerator
        );

    @BeforeEach
    void setup() {
        Map<String, String> context = new HashMap<>();
        context.put("childOrChildren", "child");
        context.put("childIsOrAre", "is");
        context.put("childWasOrWere", "was");
        context.put("localAuthorityName", LA_NAME);

        when(manageOrderDocumentService.commonContextElements(any())).thenReturn(context);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C37_EDUCATION_SUPERVISION_ORDER_DIGITAL);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void generateOrderWithTwelveMonthEndDate() {
        CaseData caseData = buildCaseDataWithDateSpecified(TWELVE_MONTHS_FROM_DATE_OF_ORDER);
        DocmosisParameters docParam = underTest.generate(caseData);

        DocmosisParameters expectedParam = buildExpectedParam(TWELVE_MONTHS_FROM_DATE_OF_ORDER);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithEndOfEducationAge() {
        CaseData caseData = buildCaseDataWithDateSpecified(UNTIL_END_OF_COMPULSORY_EDUCATION_AGE);
        DocmosisParameters docParam = underTest.generate(caseData);

        DocmosisParameters expectedParam = buildExpectedParam(UNTIL_END_OF_COMPULSORY_EDUCATION_AGE);

        assertThat(docParam).isEqualTo(expectedParam);
    }

    private CaseData buildCaseDataWithDateSpecified(ManageOrderEndDateOption endDateType) {
        return CaseData.builder()
            .caseLocalAuthority(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C37_EDUCATION_SUPERVISION_ORDER_DIGITAL)
                .manageOrdersLeaName(LEA_NAME)
                .manageOrdersEndDateWithEducationAge(endDateType)
                .manageOrdersIsByConsent(YES.getValue())
                .build())
            .build();
    }

    private DocmosisParameters buildExpectedParam(ManageOrderEndDateOption endDateType) {
        String orderMessage = "A parent of the child may be guilty of an offence "
                              + "if he or she persistently fails to comply with a direction given "
                              + "by the supervisor under this order while it is in force "
                              + "(Paragraph 18 Schedule 3 Children Act 1989). ";

        String orderDetails = "The Court was satisfied that the child was of compulsory school "
                                    + "age and was not being properly educated.\n\n"
                                    + "The Court orders " + LEA_NAME
                                    + " local education authority to supervise the child ";
        switch (endDateType) {
            case TWELVE_MONTHS_FROM_DATE_OF_ORDER:
                orderDetails += "for a period of 12 months beginning on the date of this order.";
                break;
            case UNTIL_END_OF_COMPULSORY_EDUCATION_AGE:
                orderDetails += "until the child is no longer of compulsory school age.";
                break;
        }


        return C37EducationSupervisionOrderDocmosisParameters.builder()
            .orderTitle(Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL.getTitle())
            .childrenAct(Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL.getChildrenAct())
            .orderHeader("Warning\n")
            .orderMessage(orderMessage)
            .orderByConsent("By consent")
            .orderDetails(orderDetails)
            .build();
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
