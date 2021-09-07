package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock
    private Time time;

    @Mock
    private FeeService feeService;

    @Mock
    private DocumentSealingService sealingService;

    @InjectMocks
    private PlacementService underTest;

    @Test
    void shouldPrepareListOfChildrenWithoutPlacement() {

        final CaseData caseData = CaseData.builder().build();

        final PlacementEventData actualPlacementData = underTest.init(caseData);

        final PlacementEventData expectedPlacementData = PlacementEventData.builder()
            .build();

        assertThat(actualPlacementData).isEqualTo(expectedPlacementData);


    }

}
