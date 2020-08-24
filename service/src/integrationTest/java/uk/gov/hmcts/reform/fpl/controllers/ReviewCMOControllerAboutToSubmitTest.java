package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.cmo.ReviewCMOController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewCMOControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private DocumentSealingService documentSealingService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private FeatureToggleService featureToggleService;

    private CaseManagementOrder cmo = buildCMO();
    private DocumentReference convertedDocument;
    private DocumentReference sealedDocument;

    ReviewCMOControllerAboutToSubmitTest() {
        super("review-cmo");
    }

    @BeforeEach()
    void setup() throws Exception {
        convertedDocument = testDocumentReference();
        sealedDocument = testDocumentReference();
        given(documentConversionService.convertToPdf(cmo.getOrder())).willReturn(convertedDocument);
        given(documentSealingService.sealDocument(convertedDocument)).willReturn(sealedDocument);
    }

    @Test
    void shouldSetReturnStatusAndRequestedChangesWhenJudgeRejectsOrder() {
        ReviewDecision reviewDecision = ReviewDecision.builder()
            .changesRequestedByJudge("Please change XYZ")
            .decision(JUDGE_REQUESTED_CHANGES)
            .build();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(element(cmo)))
            .reviewCMODecision(reviewDecision)
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        CaseManagementOrder returnedCMO = responseData.getDraftUploadedCMOs().get(0).getValue();

        CaseManagementOrder expectedCMO = cmo.toBuilder()
            .requestedChanges(reviewDecision.getChangesRequestedByJudge())
            .status(RETURNED)
            .build();

        assertThat(returnedCMO).isEqualTo(expectedCMO);
    }

    @Test
    void shouldSealPDFAndAddToSealedCMOsListWhenJudgeApprovesOrder() throws Exception {
        UUID cmoId = UUID.randomUUID();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingDetails(List.of(element(hearing(cmoId))))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build()).build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        CaseManagementOrder expectedSealedCmo = cmo.toBuilder()
            .order(sealedDocument)
            .dateIssued(LocalDate.now())
            .status(APPROVED)
            .build();

        assertThat(State.ISSUE_RESOLUTION).isNotEqualTo(responseData.getState());
        assertThat(responseData.getDraftUploadedCMOs()).isEmpty();
        assertThat(responseData.getSealedCMOs())
            .extracting(Element::getValue)
            .containsExactly(expectedSealedCmo);
    }

    @Test
    void shouldUpdateStateToIssueResolutionWhenNextHearingIssueResolutionAndCmoDecisionIsSendToAllParties() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        UUID cmoId = UUID.randomUUID();
        CaseData caseData = buildCaseData(cmoId, SEND_TO_ALL_PARTIES);
        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.ISSUE_RESOLUTION).isEqualTo(responseData.getState());
    }

    @Test
    void shouldNotUpdateStateToIssueResolutionWhenFeatureToggledOff() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(false);

        UUID cmoId = UUID.randomUUID();
        CaseData caseData = buildCaseData(cmoId, SEND_TO_ALL_PARTIES);
        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.ISSUE_RESOLUTION).isNotEqualTo(responseData.getState());
    }

    @Test
    void shouldNotUpdateStateToIssueResolutionWhenReviewDecisionIsNotSendToAllParties() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        UUID cmoId = UUID.randomUUID();
        CaseData caseData = buildCaseData(cmoId, JUDGE_REQUESTED_CHANGES);
        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.ISSUE_RESOLUTION).isNotEqualTo(responseData.getState());
    }

    @Test
    void shouldNotUpdateStateToIssueResolutionWhenNextHearingIsNotOfTypeIssueResolution() {
        given(featureToggleService.isNewCaseStateModelEnabled()).willReturn(true);

        UUID cmoId = UUID.randomUUID();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(List.of(element(cmoId, cmo)))
            .hearingDetails(List.of(
                element(hearing(cmoId)),
                element(HearingBooking.builder()
                    .startDate(LocalDateTime.now().plusDays(1))
                    .type(CASE_MANAGEMENT)
                    .caseManagementOrderId(UUID.randomUUID())
                    .build())))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build()).build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(State.ISSUE_RESOLUTION).isNotEqualTo(responseData.getState());
    }

    @Test
    void shouldNotModifyDataIfNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of()).build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData).isEqualTo(caseData);
    }

    private CaseData buildCaseData(UUID cmoID, CMOReviewOutcome cmoReviewOutcome) {
        return CaseData.builder()
            .draftUploadedCMOs(List.of(element(cmoID, cmo)))
            .hearingDetails(List.of(
                element(hearing(cmoID)),
                element(buildIssueResolutionHearing())))
            .reviewCMODecision(ReviewDecision.builder().decision(cmoReviewOutcome).build()).build();
    }

    private HearingBooking buildIssueResolutionHearing() {
        return HearingBooking.builder()
            .startDate(LocalDateTime.now().plusDays(1))
            .type(ISSUE_RESOLUTION)
            .caseManagementOrderId(UUID.randomUUID())
            .build();
    }

    private CaseManagementOrder buildCMO() {
        return CaseManagementOrder.builder()
            .hearing("Test hearing 25th December 2020")
            .order(testDocumentReference())
            .judgeTitleAndName("Her Honour Judge Judy")
            .status(SEND_TO_JUDGE).build();
    }

    private HearingBooking hearing(UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .caseManagementOrderId(cmoId)
            .build();
    }
}
