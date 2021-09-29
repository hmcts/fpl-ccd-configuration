package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A206PlacementOrderNotificationDocmosisParameters;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.A206;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ExtendWith(MockitoExtension.class)
class A206PlacementOrderNotificationParameterGeneratorTest {

    @Mock
    private PlacementService placementService;

    @InjectMocks
    private A206PlacementOrderNotificationParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(A70_PLACEMENT_ORDER);
    }

    @Test
    void generate() {
        Element<Child> selectedPlacementChild = element(Child.builder()
            .party(ChildParty.builder()
                .firstName("Alex")
                .lastName("White")
                .build())
            .build());
        Element<Placement> placement = element(Placement.builder()
            .childId(selectedPlacementChild.getId())
            .build());
        CaseData caseData = CaseData.builder()
            .children1(List.of(selectedPlacementChild))
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersSerialNumber("123")
                    .manageOrdersChildPlacementApplication(buildDynamicList(0, Pair.of(placement.getId(), "Placement")))
                    .build()
            )
            .build();
        when(placementService.getChildByPlacementId(caseData, placement.getId())).thenReturn(selectedPlacementChild);

        A206PlacementOrderNotificationDocmosisParameters docmosisParameters = underTest.generate(caseData);

        assertThat(docmosisParameters.getSerialNumber()).isEqualTo("123");
        assertThat(docmosisParameters.getChildrenAct()).isEqualTo("Adoption and Children Act 2002");
        assertThat(docmosisParameters.getChild().getName()).isEqualTo("Alex White");
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(A206);
    }

}
