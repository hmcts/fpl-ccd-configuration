package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerMidEventTest extends AbstractCallbackTest {

    private final String hearing1 = "Test hearing 21st August 2020";
    private final String hearing2 = "Test hearing 9th April 2021";

    private final DocumentReference orderForFirstCMO = testDocumentReference();
    private final DocumentReference orderForSecondCMO = testDocumentReference();
    private final DocumentReference orderForBlankOrder = testDocumentReference();

    private final Element<HearingOrder> agreedCMO = element(buildDraftOrder(hearing1, orderForFirstCMO, AGREED_CMO));
    private final Element<HearingOrder> draftCMO = element(buildDraftOrder(hearing2, orderForSecondCMO, DRAFT_CMO));
    private final Element<HearingOrder> draftOrder1 = element(buildDraftOrder(hearing1, orderForBlankOrder, C21));
    private final Element<HearingOrder> draftOrder2 = element(buildDraftOrder(hearing2, orderForBlankOrder, C21));

    ApproveDraftOrdersControllerMidEventTest() {
        super("approve-draft-orders");
    }

    @Test
    void shouldPopulateDraftOrdersReadyForApprovalOnReviewDecisionPageForTheSelectedHearingOrdersBundle() {
        UUID hearingOrdersBundle1 = UUID.randomUUID();
        UUID hearingOrdersBundle2 = UUID.randomUUID();

        List<Element<HearingOrdersBundle>> hearingOrdersBundles = List.of(
            buildHearingOrdersBundle(hearingOrdersBundle1, hearing1, newArrayList(agreedCMO, draftOrder1)),
            buildHearingOrdersBundle(hearingOrdersBundle2, hearing2, newArrayList(draftCMO, draftOrder2)));

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", newArrayList(agreedCMO, draftCMO),
                "hearingOrdersBundlesDrafts", hearingOrdersBundles,
                "cmoToReviewList", hearingOrdersBundle1.toString()
            )).build();

        CaseData responseData = extractCaseData(postMidEvent(caseDetails));

        ReviewDraftOrdersData expectedPageData = ReviewDraftOrdersData.builder()
            .cmoDraftOrderTitle(agreedCMO.getValue().getTitle())
            .cmoDraftOrderDocument(agreedCMO.getValue().getOrder())
            .draftCMOExists("Y")
            .draftOrder1Title(draftOrder1.getValue().getTitle())
            .draftOrder1Document(draftOrder1.getValue().getOrder())
            .draftBlankOrdersCount("1")
            .build();

        assertThat(responseData.getReviewDraftOrdersData()).isEqualTo(expectedPageData);
    }

    @Test
    void shouldNotPopulateDraftCMOOnReviewDecisionPageForTheSelectedHearingOrdersBundle() {
        UUID hearingOrdersBundle1 = UUID.randomUUID();
        UUID hearingOrdersBundle2 = UUID.randomUUID();

        List<Element<HearingOrdersBundle>> hearingOrdersBundles = List.of(
            buildHearingOrdersBundle(hearingOrdersBundle1, hearing1, newArrayList(agreedCMO)),
            buildHearingOrdersBundle(hearingOrdersBundle2, hearing2, newArrayList(draftCMO, draftOrder2)));

        CaseDetails caseDetails = CaseDetails.builder().data(
            Map.of("draftUploadedCMOs", newArrayList(agreedCMO, draftCMO),
                "hearingOrdersBundlesDrafts", hearingOrdersBundles,
                "cmoToReviewList", hearingOrdersBundle2.toString()
            )).build();

        CaseData responseData = extractCaseData(postMidEvent(caseDetails));

        DynamicList expectedCmoToReviewList = DynamicList.builder()
            .value(DynamicListElement.builder().code(hearingOrdersBundle2).label(hearing2).build())
            .listItems(hearingOrdersBundles.stream().map(bundle -> DynamicListElement.builder()
                .code(bundle.getId())
                .label(bundle.getValue().getHearingName())
                .build())
                .collect(Collectors.toList()))
            .build();

        ReviewDraftOrdersData expectedPageData = ReviewDraftOrdersData.builder()
            .draftCMOExists("N")
            .draftOrder1Title(draftOrder2.getValue().getTitle())
            .draftOrder1Document(draftOrder2.getValue().getOrder())
            .draftBlankOrdersCount("1")
            .build();

        assertThat(responseData.getReviewDraftOrdersData()).isEqualTo(expectedPageData);
        assertThat(responseData.getCmoToReviewList()).isEqualTo(
            mapper.convertValue(expectedCmoToReviewList, new TypeReference<Map<String, Object>>() {
            }));
    }

    private Element<HearingOrdersBundle> buildHearingOrdersBundle(
        UUID hearingOrdersBundle1, String hearing, List<Element<HearingOrder>> orders) {
        return element(hearingOrdersBundle1,
            HearingOrdersBundle.builder().hearingId(UUID.randomUUID())
                .orders(orders)
                .hearingName(hearing).build());
    }

    private static HearingOrder buildDraftOrder(String hearing, DocumentReference order, HearingOrderType orderType) {
        return HearingOrder.builder()
            .hearing(hearing)
            .title(hearing)
            .order(order)
            .type(orderType)
            .status(DRAFT_CMO.equals(orderType) ? DRAFT : SEND_TO_JUDGE).build();
    }
}
