package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

class GatekeepingOrderServiceTest {
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference REFERENCE = buildFromDocument(DOCUMENT);

    private final DocumentService documentService = mock(DocumentService.class);
    private final GatekeepingOrderGenerationService gatekeepingOrderGenerationService =
        mock(GatekeepingOrderGenerationService.class);

    private GatekeepingOrderService underTest;

    @BeforeEach
    void setUp() {
        underTest = new GatekeepingOrderService(documentService, gatekeepingOrderGenerationService);
        given(documentService.getDocumentFromDocmosisOrderTemplate(any(), eq(SDO))).willReturn(DOCUMENT);
    }

    @Test
    void shouldNotBuildNextStepsLabelWhenAllRequiredInformationPresent() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(null)
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoHearingDetails() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoAllocatedJudge() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("No")
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Nelson")
                    .build())
                .build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the allocated judge")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoIssuingJudge() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenAllFieldsMandatoryFieldsMissing() {
        CaseData caseData = CaseData.builder().build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(REFERENCE)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details\n\n"
                + "* the allocated judge\n\n"
                + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData)).isEqualTo(expected);
    }

    @Test
    void shouldSetAllocatedJudgeLabel() {
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .build();
        JudgeAndLegalAdvisor issuingJudge = JudgeAndLegalAdvisor.builder()
            .build();

        JudgeAndLegalAdvisor expectedJudge = issuingJudge.toBuilder()
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Hastings")
            .build();

        assertThat(underTest.setAllocatedJudgeLabel(allocatedJudge, issuingJudge)).isEqualTo(expectedJudge);
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithNoOthers() {
        CaseData caseData = CaseData.builder().build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6));
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithOthers() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(mock(Other.class)).build()).build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6, C6A));
    }
}
