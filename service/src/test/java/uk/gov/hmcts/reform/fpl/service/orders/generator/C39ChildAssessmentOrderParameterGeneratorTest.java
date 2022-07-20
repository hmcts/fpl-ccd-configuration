package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersChildAssessmentType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.ManageOrderDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C39ChildAssessmentOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        CaseData caseData = buildCaseDataWithAssessmentType(MEDICAL_ASSESSMENT, false, 7, false);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(MEDICAL_ASSESSMENT, false, 7, false);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithPsychiatricAssessmentAnd7Days() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT, false,7, false);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, false, 7, false);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithMedicalAssessmentAnd1Day() {
        CaseData caseData = buildCaseDataWithAssessmentType(MEDICAL_ASSESSMENT, false, 1, false);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(MEDICAL_ASSESSMENT, false, 1, false);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithPsychiatricAssessmentAnd1Day() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT, false,1, false);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, false, 1, false);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithChildKeepAwayFromHome() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT, true,1, false);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, true, 1, false);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    @Test
    void generateOrderWithCostOrderExist() {
        CaseData caseData = buildCaseDataWithAssessmentType(PSYCHIATRIC_ASSESSMENT, false,1, true);
        DocmosisParameters docParam = underTest.generate(caseData);
        DocmosisParameters expectedParam = buildExpectedParam(PSYCHIATRIC_ASSESSMENT, false, 1, true);
        assertThat(docParam).isEqualTo(expectedParam);
    }

    private CaseData buildCaseDataWithAssessmentType(ManageOrdersChildAssessmentType assessmentType,
                                                     boolean childKeepAwayFromHome, int duration,
                                                     boolean isCostOrderExist) {
        ManageOrdersEventData.ManageOrdersEventDataBuilder eventBuilder = ManageOrdersEventData.builder()
            .manageOrdersType(C39_CHILD_ASSESSMENT_ORDER)
            .manageOrdersAssessmentStartDate(LocalDate.of(2022, Month.JUNE, 4))
            .manageOrdersChildAssessmentType(assessmentType)
            .manageOrdersDurationOfAssessment(duration)
            .whichChildIsTheOrderFor(TestDataHelper.buildDynamicList(0, Pair.of(UUID.randomUUID(), "Carrie Lam")))
            .manageOrdersIsByConsent(YES.getValue())
            .manageOrdersPlaceOfAssessment("Place Of Assessment")
            .manageOrdersAssessingBody("Assessing Body")
            .manageOrdersChildKeepAwayFromHome(YesNo.from(childKeepAwayFromHome))
            .manageOrdersDoesCostOrderExist(YesNo.from(isCostOrderExist));

        if (childKeepAwayFromHome) {
            eventBuilder
                .manageOrdersFullAddressToStayIfKeepAwayFromHome(Address.builder()
                    .addressLine1("addressLine1")
                    .addressLine2("addressLine2")
                    .addressLine3("addressLine3")
                    .country("country")
                    .postcode("postcode")
                    .postTown("postTown")
                    .county("county")
                    .build())
                .manageOrdersStartDateOfStayIfKeepAwayFromHome(LocalDate.of(2022, 6, 1))
                .manageOrdersEndDateOfStayIfKeepAwayFromHome(LocalDate.of(2023, 6, 1))
                .manageOrdersChildFirstContactIfKeepAwayFromHome("Child First Contact")
                .manageOrdersChildSecondContactIfKeepAwayFromHome("Child Second Contact")
                .manageOrdersChildThirdContactIfKeepAwayFromHome("Child Third Contact");
        }

        if (isCostOrderExist) {
            eventBuilder.manageOrdersCostOrderDetails("Cost Order Details");
        }

        return CaseData.builder()
            .caseLocalAuthority(LA_NAME)
            .manageOrdersEventData(eventBuilder.build())
            .build();
    }

    private DocmosisParameters buildExpectedParam(ManageOrdersChildAssessmentType assessmentType,
                                                  boolean childKeepAwayFromHome, int duration,
                                                  boolean isCostOrderExist) {
        String orderDetails = "The Court orders a %s of the child.\n\n"
            + "The Court directs that the child is to be assessed at Place Of Assessment. "
            + "The child is to be assessed by Assessing Body.\n\n";

        if (childKeepAwayFromHome) {
            orderDetails += "The child may be kept away from home and stay at addressLine1, addressLine2, "
                + "addressLine3, postTown, county, postcode, country from 1st June 2022 to 1st June 2023.\n"
                + "While away from home, the child must be allowed to contact with\n"
                + "Child First Contact\nChild Second Contact\nChild Third Contact\n\n";
        }

        orderDetails += "The assessment is to begin by "
            + "4th June 2022 and last no more than %s day%s from the date it begins.\n\n"
            + "Notice: Any person who is in a position to produce the child must do so to "
            + "an office of the local authority "
            + "and must comply with the directions in this order.";

        if (isCostOrderExist) {
            orderDetails += "\n\nCost Order Details";
        }

        return C39ChildAssessmentOrderDocmosisParameters.builder()
            .orderTitle(C39_CHILD_ASSESSMENT_ORDER.getTitle())
            .childrenAct(C39_CHILD_ASSESSMENT_ORDER.getChildrenAct())
            .orderByConsent("By consent")
            .orderDetails(String.format(orderDetails, assessmentType.getTitle(), duration, duration > 1 ? "s" : ""))
            .build();
    }
}
