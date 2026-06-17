package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPLICANT_CHANGE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPROVE_APPLICATION_AND_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.APPROVE_APPLICATION_CHANGE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions.REFUSE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReviewAdditionalApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewAdditionalApplicationControllerMidEventTest extends AbstractCallbackTest {
    private static final UUID DRAFT_ORDER_ID = UUID.randomUUID();

    @MockBean
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    @MockBean
    private ApproveDraftOrdersService approveDraftOrdersService;

    @MockBean
    private HearingOrderGenerator hearingOrderGenerator;

    private static final C2AdditionalApplicationEventData C2_APPLICATION =
        C2AdditionalApplicationEventData.builder()
        .build();

    private static final OtherApplicationsBundle OTHER_APPLICATION =
        OtherApplicationsBundle.builder().applicantName("TEST").build();

    private static final Element<AdditionalApplicationsBundle> APPLICATION_BUNDLE_ELEMENT =
        element(AdditionalApplicationsBundle.builder()
            .uploadedDateTime("1 January 2021, 12:00pm")
            .author("TESTING")
            .c2DocumentBundle(C2_APPLICATION)
            .otherApplicationsBundle(OTHER_APPLICATION)
            .build());

    ReviewAdditionalApplicationControllerMidEventTest() {
        super("review-additional-application");
    }

    @BeforeEach
    void initTest() {
        when(reviewAdditionalApplicationService.getSelectedApplicationsToBeReviewed(any()))
            .thenReturn(APPLICATION_BUNDLE_ELEMENT);
        when(reviewAdditionalApplicationService
            .initReviewFieldsForSelectedBundle(APPLICATION_BUNDLE_ELEMENT.getValue()))
            .thenReturn(Map.of("c2AdditionalApplicationToBeReview", C2_APPLICATION,
                "otherAdditionalApplicationToBeReview", OTHER_APPLICATION));
    }

    @Test
    void shouldQuerySelectedAdditionalApplicationBundle() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseData.builder().build());
        CaseData resultCaseData = extractCaseData(response);
        ConfirmApplicationReviewedEventData resultEventData = resultCaseData.getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getC2AdditionalApplicationToBeReview()).isEqualTo(C2_APPLICATION);
        assertThat(resultEventData.getOtherAdditionalApplicationToBeReview()).isEqualTo(OTHER_APPLICATION);
    }

    @Test
    void shouldGeneratePreviewForApproveApplicationAndOrderRoute() {
        DocumentReference draftOrderDocument = document("draft-order.docx");
        DocumentReference previewOrderDocument = document("draft-order-with-coversheet.pdf");

        CaseData caseData = CaseData.builder()
            .approveAdditionalAppRouter(APPROVE_APPLICATION_AND_ORDER)
            .confirmApplicationReviewedEventData(ConfirmApplicationReviewedEventData.builder()
                .c2AdditionalApplicationToBeReview(C2AdditionalApplicationEventData.builder()
                    .draftOrdersBundle(List.of(element(
                        DRAFT_ORDER_ID,
                        DraftOrder.builder().title("Draft order title").document(draftOrderDocument).build()
                    )))
                    .build())
                .build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .build();

        when(approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(any())).thenReturn("District Judge Example");
        when(hearingOrderGenerator.addCoverSheet(any(), eq(draftOrderDocument))).thenReturn(previewOrderDocument);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "edit-hearing");
        CaseData resultCaseData = extractCaseData(response);
        ConfirmApplicationReviewedEventData resultEventData = resultCaseData.getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getReviewOrderUrgency()).isEqualTo(YesNo.YES);
        assertThat(resultEventData.getAddCoverSheet()).isEqualTo(YesNo.YES);
        assertThat(response.getData().get("previewApprovedOrder1")).isEqualTo(Map.of(
            "document_url", previewOrderDocument.getUrl(),
            "document_filename", previewOrderDocument.getFilename(),
            "document_binary_url", previewOrderDocument.getBinaryUrl()
        ));
        assertThat(response.getData().get("previewApprovedOrderTitle1")).isEqualTo("Order Draft order title");
    }

    @Test
    void shouldSetFlagsForApproveApplicationChangeOrderRoute() {
        CaseData caseData = CaseData.builder()
            .approveAdditionalAppRouter(APPROVE_APPLICATION_CHANGE_ORDER)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "edit-hearing");
        ConfirmApplicationReviewedEventData resultEventData = extractCaseData(response)
            .getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getReviewOrderUrgency()).isEqualTo(YesNo.YES);
        assertThat(resultEventData.getAddCoverSheet()).isEqualTo(YesNo.NO);
    }

    @Test
    void shouldSetFlagsForApplicantChangeOrderRoute() {
        CaseData caseData = CaseData.builder()
            .approveAdditionalAppRouter(APPLICANT_CHANGE_ORDER)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "edit-hearing");
        ConfirmApplicationReviewedEventData resultEventData = extractCaseData(response)
            .getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getReviewOrderUrgency()).isEqualTo(YesNo.NO);
        assertThat(resultEventData.getAddCoverSheet()).isEqualTo(YesNo.NO);
    }

    @Test
    void shouldSetFlagsForDefaultRoute() {
        CaseData caseData = CaseData.builder()
            .approveAdditionalAppRouter(REFUSE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "edit-hearing");
        ConfirmApplicationReviewedEventData resultEventData = extractCaseData(response)
            .getConfirmApplicationReviewedEventData();

        assertThat(resultEventData.getReviewOrderUrgency()).isEqualTo(YesNo.NO);
        assertThat(resultEventData.getAddCoverSheet()).isEqualTo(YesNo.NO);
    }

    private static DocumentReference document(String filename) {
        return DocumentReference.builder()
            .url("http://dm-store/documents/" + filename)
            .binaryUrl("http://dm-store/documents/" + filename + "/binary")
            .filename(filename)
            .build();
    }

}
