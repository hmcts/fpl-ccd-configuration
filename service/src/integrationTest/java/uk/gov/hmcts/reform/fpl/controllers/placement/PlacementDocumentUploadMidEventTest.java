package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feeResponse;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementDocumentUploadMidEventTest extends AbstractCallbackTest {

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    PlacementDocumentUploadMidEventTest() {
        super("placement");
    }

    @Test
    void shouldFetchPlacementFeeWhenPaymentIsRequired() {

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder().build())
            .build();

        when(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "Placement", "adoption"))
            .thenReturn(feeResponse(455.5));

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "documents-upload"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementFee()).isEqualTo("45550");
    }

    @Test
    void shouldNoFetchPlacementFeeWhenPaymentHasBeenAlreadyTakenOnSameDay() {

        final LocalDateTime earlierToday = dateNow().atTime(0, 0, 1);

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementLastPaymentTime(earlierToday)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "documents-upload"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementFee()).isNull();

        verifyNoMoreInteractions(feesRegisterApi);
    }

}
