package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feeResponse;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticesUploadMidEventTest extends AbstractPlacementControllerTest {

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @Test
    void shouldReturnErrorsWhenRequiredDocumentsNotPresent() {

        final DynamicList parentList1 = dynamicLists.from(0,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final DynamicList parentList2 = dynamicLists.from(0,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final PlacementEventData placementData = PlacementEventData.builder()
            .placement(Placement.builder().build())
            .placementNoticeForFirstParentParentsList(parentList1)
            .placementNoticeForSecondParentParentsList(parentList2)
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2))
            .placementEventData(placementData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "notices-upload");

        assertThat(response.getErrors()).containsExactly("First and second parents can not be same");

        verifyNoMoreInteractions(feesRegisterApi);
    }

    @Test
    void shouldFetchPlacementFeeWhenPaymentIsRequired() {

        final DynamicList parentList1 = dynamicLists.from(0,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final DynamicList parentList2 = dynamicLists.from(1,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final PlacementEventData placementData = PlacementEventData.builder()
            .placement(Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .build())
            .placementNoticeForFirstParentParentsList(parentList1)
            .placementNoticeForSecondParentParentsList(parentList2)
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2))
            .placementEventData(placementData)
            .build();

        when(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "Placement", "adoption"))
            .thenReturn(feeResponse(455.5));

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "notices-upload"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementFee()).isEqualTo("45550");
    }

    @Test
    void shouldNoFetchPlacementFeeWhenPaymentHasBeenAlreadyTakenOnSameDay() {

        final DynamicList parentList1 = dynamicLists.from(0,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final DynamicList parentList2 = dynamicLists.from(1,
            Pair.of("Emma Green - mother", respondent1.getId()),
            Pair.of("Adam Green - father", respondent2.getId()));

        final LocalDateTime earlierToday = dateNow().atTime(0, 0, 1);

        final PlacementEventData placementData = PlacementEventData.builder()
            .placementLastPaymentTime(earlierToday)
            .placement(Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .build())
            .placementNoticeForFirstParentParentsList(parentList1)
            .placementNoticeForSecondParentParentsList(parentList2)
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2))
            .placementEventData(placementData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "notices-upload"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementFee()).isNull();

        verifyNoMoreInteractions(feesRegisterApi);
    }

}
