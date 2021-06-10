package uk.gov.hmcts.reform.fpl.service.sdo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseConverter.class})
class GatekeepingOrderServiceTest {
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    @Mock
    private OrdersLookupService ordersLookupService;

    @InjectMocks
    private GatekeepingOrderService underTest;

    @Test
    void shouldNotBuildNextStepsLabelWhenAllRequiredInformationPresent() {
        Document document = testDocument();
        DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(reference)
            .orderStatus(null)
            .nextSteps(null)
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData, document)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoHearingDetails() {
        Document document = testDocument();
        DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
                .build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(reference)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData, document)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoAllocatedJudge() {
        Document document = testDocument();
        DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

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
            .draftDocument(reference)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the allocated judge")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData, document)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenNoIssuingJudge() {
        Document document = testDocument();
        DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build()))
            .allocatedJudge(Judge.builder().judgeLastName("Judy").build())
            .build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(reference)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData, document)).isEqualTo(expected);
    }

    @Test
    void shouldBuildNextStepsLabelWhenAllFieldsMandatoryFieldsMissing() {
        Document document = testDocument();
        DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

        CaseData caseData = CaseData.builder().build();

        SaveOrSendGatekeepingOrder expected = SaveOrSendGatekeepingOrder.builder()
            .draftDocument(reference)
            .orderStatus(null)
            .nextSteps(NEXT_STEPS + "* the first hearing details\n\n"
                + "* the allocated judge\n\n"
                + "* the judge issuing the order")
            .build();

        assertThat(underTest.buildSaveOrSendPage(caseData, document)).isEqualTo(expected);
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
