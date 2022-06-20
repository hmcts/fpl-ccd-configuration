package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersChildAssessmentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C39ChildAssessmentOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersChildAssessmentType.MEDICAL_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersChildAssessmentType.PSYCHIATRIC_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C39_CHILD_ASSESSMENT_ORDER;


class C39ChildAssessmentOrderParameterGeneratorTest {
    private static final String LA_NAME = "Testing LA Name";
    private final ManageOrderDocumentService manageOrderDocumentService = mock(ManageOrderDocumentService.class);
    private final OrderMessageGenerator orderMessageGenerator = new OrderMessageGenerator(manageOrderDocumentService);

    private C39ChildAssessmentOrderParameterGenerator underTest =
        new C39ChildAssessmentOrderParameterGenerator(orderMessageGenerator);

    @BeforeEach
    void setup() {
        Map<String, String> context = new HashMap<>();
        when(manageOrderDocumentService.commonContextElements(any())).thenReturn(context);
    }

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C39_CHILD_ASSESSMENT_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER_V2);
    }

    @Test
    void generateOrderWithMedicalAssessmentAnd7Days() {
        CaseData caseData = buildCaseDataWithAssessmentType(MEDICAL_ASSESSMENT, 7);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(MEDICAL_ASSESSMENT, 7);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithPsychiatricAssessmentAnd7Days() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT,7);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, 7);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithMedicalAssessmentAnd1Day() {
        CaseData caseData = buildCaseDataWithAssessmentType(MEDICAL_ASSESSMENT, 1);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(MEDICAL_ASSESSMENT, 1);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithPsychiatricAssessmentAnd1Day() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT,1);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, 1);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    private CaseData buildCaseDataWithAssessmentType(ManageOrdersChildAssessmentType assessmentType, int duration) {
        return CaseData.builder()
            .caseLocalAuthority(LA_NAME)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C39_CHILD_ASSESSMENT_ORDER)
                .manageOrdersAssessmentStartDate(LocalDate.of(2022, Month.JUNE, 4))
                .manageOrdersChildAssessmentType(assessmentType)
                .manageOrdersDurationOfAssessment(duration)
                .manageOrdersIsByConsent(YES.getValue())
                .build())
            .build();
    }

    private DocmosisParameters buildExpectedParam(ManageOrdersChildAssessmentType assessmentType,
        int duration) {
        String orderDetails = "The Court orders a %s of a child.\n\nThe assessment is to begin by "
            + "4th June 2022 and last no more than %s day%s from the date it begins.\n\n"
            + "Notice: Any person who is in a position to produce the child must do so to Carrie Lam "
            + "and must comply with the directions in this order.";

        return C39ChildAssessmentOrderDocmosisParameters.builder()
            .orderTitle(C39_CHILD_ASSESSMENT_ORDER.getTitle())
            .childrenAct(C39_CHILD_ASSESSMENT_ORDER.getChildrenAct())
            .orderByConsent("By consent")
            .orderDetails(String.format(orderDetails, assessmentType.getTitle(), duration, duration > 1 ? "s" : ""))
            .build();
    }
}