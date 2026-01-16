package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.AllocateJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudgeType.LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerPreviewOrderMidEventTest extends AbstractCallbackTest {
    private static final DocumentReference DRAFT_ORDER = testDocumentReference("draft-order.pdf");
    private static final DocumentReference DRAFT_OREDR_WITH_COVERSHEET =
        testDocumentReference("draft-order-with-cover.pdf");

    private static final Element<HearingOrder> DRAFT_ORDER_ELEMENT = element(HearingOrder.builder()
        .order(DRAFT_ORDER).title("TestingTitle").type(HearingOrderType.C21).status(CMOStatus.SEND_TO_JUDGE).build());

    private static final UUID SELECTED_HEARING_ID = UUID.randomUUID();
    private static final Element<HearingOrdersBundle> ORDER_BUNDLE = element(SELECTED_HEARING_ID,
        HearingOrdersBundle.builder().hearingId(UUID.randomUUID())
            .orders(new ArrayList<>((List.of(DRAFT_ORDER_ELEMENT)))).build());

    @MockBean
    private HearingOrderGenerator hearingOrderGenerator;

    ApproveDraftOrdersControllerPreviewOrderMidEventTest() {
        super("approve-draft-orders/preview-orders");
    }

    @BeforeEach
    void setUp() {
        when(hearingOrderGenerator.addCoverSheet(any(), eq(DRAFT_ORDER))).thenReturn(DRAFT_OREDR_WITH_COVERSHEET);
    }

    @Test
    void shouldPopulatePreviewApprovedOrderIfC2() {
        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(ORDER_BUNDLE))
            .allocateJudgeEventData(new AllocateJudgeEventData(LEGAL_ADVISOR, null, null,
                Judge.builder().judgeFullName("Judge John").build()))
            .selectedHearingId(SELECTED_HEARING_ID)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder()
                .reviewDecision1(ReviewDecision.builder()
                    .decision(CMOReviewOutcome.SEND_TO_ALL_PARTIES)
                    .build())
                .build())
            .build();

        Map<String, Object> responseData = postMidEvent(caseData).getData();

        assertThat(responseData.get("previewApprovedOrder1")).extracting("document_filename")
            .isEqualTo(DRAFT_OREDR_WITH_COVERSHEET.getFilename());
        assertThat(responseData.get("previewApprovedOrder1")).extracting("document_url")
            .isEqualTo(DRAFT_OREDR_WITH_COVERSHEET.getUrl());
        assertThat(responseData.get("previewApprovedOrder1")).extracting("document_binary_url")
            .isEqualTo(DRAFT_OREDR_WITH_COVERSHEET.getBinaryUrl());
        assertThat(responseData.get("previewApprovedOrderTitle1")).isEqualTo("Order 1 TestingTitle");
    }


    @Test
    void shouldPopulatePreviewApprovedOrderIfNoC2() {
        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(ORDER_BUNDLE))
            .allocateJudgeEventData(new AllocateJudgeEventData(LEGAL_ADVISOR, null, null,
                Judge.builder().judgeFullName("Judge John").build()))
            .selectedHearingId(SELECTED_HEARING_ID)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder()
                .reviewDecision1(ReviewDecision.builder()
                    .decision(CMOReviewOutcome.REVIEW_LATER)
                    .build())
                .build())
            .build();

        Map<String, Object> responseData = postMidEvent(caseData).getData();

        assertThat(responseData).doesNotContainKey("previewApprovedOrder1");
        assertThat(responseData).doesNotContainKey("previewApprovedOrderTitle1");
    }
}
