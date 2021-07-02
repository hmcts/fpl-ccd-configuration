package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.EnglandOffices;
import uk.gov.hmcts.reform.fpl.enums.WalesOffices;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C47ADocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C47AAppointmentOfAChildrensGuardianParameterGeneratorTest {
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String CHILD = "child";
    private static final String CHILDREN = "children";

    private C47AAppointmentOfAChildrensGuardianParameterGenerator
        underTest = new C47AAppointmentOfAChildrensGuardianParameterGenerator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void shouldBuildOrderDetailsWhenAnEnglandCafcassOfficeHasBeenSelected() {
        EnglandOffices office = EnglandOffices.BOURNEMOUTH;

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersCafcassRegion("ENGLAND")
                .manageOrdersCafcassOfficesEngland(office)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .children1(wrapElements(Child.builder().build()))
            .build();

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(buildOrderDetailsLabel(office.getLabel(), CHILD))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildOrderDetailsWhenAWalesCafcassOfficeHasBeenSelected() {
        WalesOffices office = WalesOffices.LLANDRINDOD_WELLS;

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersCafcassRegion("WALES")
                .manageOrdersCafcassOfficesWales(office)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .children1(wrapElements(Child.builder().build()))
            .build();

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(buildOrderDetailsLabel(office.getLabel(), CHILD))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldUseChildrenGrammarForOrderDetailsWhenMultipleChildren() {
        EnglandOffices office = EnglandOffices.BOURNEMOUTH;

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersCafcassRegion("ENGLAND")
                .manageOrdersCafcassOfficesEngland(office)
                .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
                .build())
            .children1(wrapElements(Child.builder().build(), Child.builder().build()))
            .build();

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters()
            .orderDetails(buildOrderDetailsLabel(office.getLabel(), CHILDREN))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }



    private C47ADocmosisParameters.C47ADocmosisParametersBuilder<?,?> expectedCommonParameters() {
        return C47ADocmosisParameters.builder()
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderTitle(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.getTitle());
    }

    private String buildOrderDetailsLabel(String region, String childGrammar) {
        return String.format("The court appoints Cafcass %s as a children's guardian for the %s in the proceedings.",
            region, childGrammar);
    }
}
