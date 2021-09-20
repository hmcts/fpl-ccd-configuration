package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_PLACEMENT_APPLICATIONS;

@ExtendWith(MockitoExtension.class)
class ChildPlacementOrderPrePopulatorTest {

    private final UUID mockedChildApplicationId = UUID.randomUUID();
    private Element<String> mockedChildApplication = Element.<String>builder()
        .id(mockedChildApplicationId)
        .value("Mocked child application")
        .build();

    @Mock
    private PlacementService placementService;

    @InjectMocks
    private ChildPlacementOrderPrePopulator childPlacementOrderPrePopulator;

    @Test
    void accept() {
        assertThat(childPlacementOrderPrePopulator.accept()).isEqualTo(CHILD_PLACEMENT_APPLICATIONS);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder().id(TEST_CASE_ID).build();
        when(placementService.getPlacements(caseData)).thenReturn(List.of(mockedChildApplication));

        Map<String, Object> prePopulatedValues = childPlacementOrderPrePopulator.prePopulate(caseData);

        assertThat(prePopulatedValues)
            .extractingByKey("manageOrdersChildPlacementApplication", DynamicListAssert.getInstanceOfAssertFactory())
            .hasNoSelectedValue()
            .hasElements(Pair.of(mockedChildApplicationId, "Mocked child application"));
    }

}
