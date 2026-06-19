package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;
import uk.gov.hmcts.reform.fpl.service.markdown.ReviewAdditionalApplicationMarkdownService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPLICANT_CHANGE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPROVE_APPLICATION_AND_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.REFUSE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReviewAdditionalApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewAdditionalApplicationControllerPostSubmitAboutToSubmitTest extends AbstractCallbackTest {
    private static final UUID DRAFT_ORDER_ID = UUID.randomUUID();
    private static final String CONFIDENTIAL_APPLICATION = "Yes - only HMCTS will be able to view this application";

    @MockBean
    private ApproveDraftOrdersService approveDraftOrdersService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private ReviewAdditionalApplicationMarkdownService markdownService;

    @MockBean
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    @MockBean
    private HearingOrderGenerator hearingOrderGenerator;

    ReviewAdditionalApplicationControllerPostSubmitAboutToSubmitTest() {
        super("review-additional-application");
    }

    @Test
    void shouldApproveAndSealOrderForNonConfidentialC2() {
        CaseData caseData = buildCaseData(APPROVE_APPLICATION_AND_ORDER, false);

        doAnswer(i -> {
            Map<String, Object> data = i.getArgument(1);
            data.put("orderCollection", "updated-order-collection");
            return null;
        }).when(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());

        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(any(), any()))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        AboutToStartOrSubmitCallbackResponse response = postPostSubmitAboutToSubmit(caseData);

        verify(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());
        verify(approveDraftOrdersService).updateHearingDraftOrdersBundle(any(), any());
        assertThat(response.getData().get("orderCollection")).isEqualTo("updated-order-collection");
        assertThat(response.getData()).doesNotContainKeys(
            "approveAdditionalAppRouter",
            "judgeNameAndTitle",
            "reviewAdditionalAppDraftOrderId",
            "reviewAdditionalAppIsConfidential"
        );
    }

    @Test
    void shouldApproveAndSealOrderForConfidentialC2() {
        CaseData caseData = buildCaseData(APPROVE_APPLICATION_AND_ORDER, true);

        doAnswer(i -> {
            Map<String, Object> data = i.getArgument(1);
            data.put("orderCollection", "updated-order-collection");
            return null;
        }).when(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());

        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(any(), any()))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        AboutToStartOrSubmitCallbackResponse response = postPostSubmitAboutToSubmit(caseData);

        verify(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());
        assertThat(response.getData().get("orderCollection")).isEqualTo("updated-order-collection");
        assertThat(response.getData()).doesNotContainKeys(
            "approveAdditionalAppRouter",
            "judgeNameAndTitle",
            "reviewAdditionalAppDraftOrderId",
            "reviewAdditionalAppIsConfidential"
        );
    }

    @Test
    void shouldApproveAndSealOrderForConfidentialRespondentC2() {
        CaseData caseData = buildCaseData(APPROVE_APPLICATION_AND_ORDER, true, true);

        doAnswer(i -> {
            Map<String, Object> data = i.getArgument(1);
            data.put("orderCollection", "updated-order-collection");
            return null;
        }).when(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());

        when(approveDraftOrdersService.updateHearingDraftOrdersBundle(any(), any()))
            .thenReturn(Map.of("hearingOrdersBundlesDrafts", List.of()));

        AboutToStartOrSubmitCallbackResponse response = postPostSubmitAboutToSubmit(caseData);

        verify(approveDraftOrdersService).approveAndSealDraftOrder(any(), any(), any(), any(), any());
        assertThat(response.getData().get("orderCollection")).isEqualTo("updated-order-collection");
    }

    @Test
    void shouldSkipApprovalWhenRouterIsNotApproveApplicationAndOrder() {
        CaseData caseData = buildCaseData(REFUSE, false);

        AboutToStartOrSubmitCallbackResponse response = postPostSubmitAboutToSubmit(caseData);

        verify(approveDraftOrdersService, never()).approveAndSealDraftOrder(any(), any(), any(), any(), any());
        verify(approveDraftOrdersService, never()).updateHearingDraftOrdersBundle(any(), any());
        assertThat(response.getData()).doesNotContainKeys(
            "approveAdditionalAppRouter",
            "judgeNameAndTitle",
            "reviewAdditionalAppDraftOrderId",
            "reviewAdditionalAppIsConfidential"
        );
    }

    @Test
    void shouldMoveOrderToRejectedCollectionWhenApplicantMustChangeOrder() {
        CaseData caseData = buildCaseData(APPLICANT_CHANGE_ORDER, false);

        when(reviewAdditionalApplicationService.returnDraftOrderToApplicant(any(), any(), any()))
            .thenReturn(Map.of("refusedHearingOrders", List.of("rejected-order")));

        AboutToStartOrSubmitCallbackResponse response = postPostSubmitAboutToSubmit(caseData);

        verify(reviewAdditionalApplicationService).returnDraftOrderToApplicant(any(), any(), any());
        verify(approveDraftOrdersService, never()).approveAndSealDraftOrder(any(), any(), any(), any(), any());
        assertThat(response.getData().get("refusedHearingOrders")).isEqualTo(List.of("rejected-order"));
        assertThat(response.getData()).doesNotContainKeys(
            "approveAdditionalAppRouter",
            "judgeNameAndTitle",
            "reviewAdditionalAppDraftOrderId",
            "reviewAdditionalAppIsConfidential"
        );
    }

    private AboutToStartOrSubmitCallbackResponse postPostSubmitAboutToSubmit(CaseData caseData) {
        Map<String, Object> body = postMetadataCallback(
            "/callback/review-additional-application/post-submit-callback/about-to-submit",
            toCallBackRequest(caseData, CaseData.builder().build())
        );

        return mapper.convertValue(body, AboutToStartOrSubmitCallbackResponse.class);
    }

    private CaseData buildCaseData(ApproveAdditionalAppOptions router, boolean confidential) {
        return buildCaseData(router, confidential, false);
    }

    private CaseData buildCaseData(ApproveAdditionalAppOptions router,
                                   boolean confidential,
                                   boolean confidentialInRespondentCollection) {
        DocumentReference draftDocument = DocumentReference.builder()
            .url("http://dm-store/documents/draft-order.docx")
            .binaryUrl("http://dm-store/documents/draft-order.docx/binary")
            .filename("draft-order.docx")
            .build();

        Element<DraftOrder> draftOrder = element(
            DRAFT_ORDER_ID,
            DraftOrder.builder().title("Draft order").document(draftDocument).build()
        );

        C2AdditionalApplicationEventData c2Data = C2AdditionalApplicationEventData.builder()
            .confidentialApplication(confidential ? CONFIDENTIAL_APPLICATION : "No")
            .draftOrdersBundle(List.of(draftOrder))
            .build();

        Element<HearingOrder> hearingOrder = confidential
            ? element(DRAFT_ORDER_ID, HearingOrder.builder().orderConfidential(draftDocument).build())
            : element(DRAFT_ORDER_ID, HearingOrder.builder().order(draftDocument).build());

        HearingOrdersBundle.HearingOrdersBundleBuilder hearingOrdersBundleBuilder = HearingOrdersBundle.builder()
            .orders(new ArrayList<>())
            .ordersCTSC(new ArrayList<>());

        if (confidential) {
            if (confidentialInRespondentCollection) {
                hearingOrdersBundleBuilder.ordersResp0(new ArrayList<>(List.of(hearingOrder)));
            } else {
                hearingOrdersBundleBuilder.ordersCTSC(new ArrayList<>(List.of(hearingOrder)));
            }
        } else {
            hearingOrdersBundleBuilder.orders(new ArrayList<>(List.of(hearingOrder)));
        }

        Element<HearingOrdersBundle> hearingOrdersBundle = element(hearingOrdersBundleBuilder.build());

        return CaseData.builder()
            .approveAdditionalAppRouter(router)
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .judgeNameAndTitle("District Judge Example")
                .c2AdditionalApplicationToBeReview(c2Data)
                .reviewAdditionalAppDraftOrderId(DRAFT_ORDER_ID.toString())
                .reviewAdditionalAppIsConfidential(confidential ? YES : NO)
                .build())
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .build();
    }
}

