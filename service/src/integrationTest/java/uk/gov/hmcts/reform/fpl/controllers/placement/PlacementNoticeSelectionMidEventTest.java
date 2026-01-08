package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.PbaService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feeResponse;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticeSelectionMidEventTest extends AbstractPlacementControllerTest {

    public static final String EMMA_GREEN_MOTHER = "Emma Green - mother";
    public static final String ADAM_GREEN_FATHER = "Adam Green - father";

    @MockBean
    private PbaService pbaService;

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @Test
    void shouldFetchPlacementFeeWhenPaymentIsRequired() {
        final DynamicList expectedPbaList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code("PBA1234567")
                .build())
            .build();

        final DynamicList parentList1 = dynamicLists.from(0,
            Pair.of(EMMA_GREEN_MOTHER, mother.getId()),
            Pair.of(ADAM_GREEN_FATHER, father.getId()));

        final DynamicList parentList2 = dynamicLists.from(1,
            Pair.of(EMMA_GREEN_MOTHER, mother.getId()),
            Pair.of(ADAM_GREEN_FATHER, father.getId()));

        final PlacementEventData placementData = PlacementEventData.builder()
            .placement(Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(mother, father))
            .placementEventData(placementData)
            .build();

        when(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "Placement", "adoption"))
            .thenReturn(feeResponse(455.5));

        given(pbaService.populatePbaDynamicList("")).willReturn(expectedPbaList);

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "notices-respondents"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(YES);
        assertThat(actualPlacementData.getPlacementFee()).isEqualTo("45550");
        assertThat(actualPlacementData.getPlacementPayment().getPbaNumberDynamicList()).isEqualTo(expectedPbaList);
    }

    @Test
    void shouldNoFetchPlacementFeeWhenPaymentHasBeenAlreadyTakenOnSameDay() {

        final DynamicList parentList1 = dynamicLists.from(0,
            Pair.of(EMMA_GREEN_MOTHER, mother.getId()),
            Pair.of(ADAM_GREEN_FATHER, father.getId()));

        final DynamicList parentList2 = dynamicLists.from(1,
            Pair.of(EMMA_GREEN_MOTHER, mother.getId()),
            Pair.of(ADAM_GREEN_FATHER, father.getId()));

        final LocalDateTime earlierToday = dateNow().atTime(0, 0, 1);

        final PlacementEventData placementData = PlacementEventData.builder()
            .placementLastPaymentTime(earlierToday)
            .placement(Placement.builder()
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .confidentialDocuments(wrapElements(annexB))
                .build())
            .placementRespondentsToNotify(Collections.emptyList())
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(mother, father))
            .placementEventData(placementData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "notices-respondents"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementPaymentRequired()).isEqualTo(NO);
        assertThat(actualPlacementData.getPlacementFee()).isNull();

        verifyNoMoreInteractions(feesRegisterApi);
    }

}
