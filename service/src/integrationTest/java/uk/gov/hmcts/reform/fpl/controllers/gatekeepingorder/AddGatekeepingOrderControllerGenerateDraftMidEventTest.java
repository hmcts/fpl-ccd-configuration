package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerGenerateDraftMidEventTest extends AbstractCallbackTest {
    AddGatekeepingOrderControllerGenerateDraftMidEventTest() {
        super("add-gatekeeping-order");
    }

    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(DOCUMENT);
    private static final String NEXT_STEPS = "## Next steps\n\n"
        + "Your order will be saved as a draft in 'Draft orders'.\n\n"
        + "You cannot seal and send the order until adding:\n\n";

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @BeforeEach
    void setup() {
        final byte[] pdf = testDocumentBinaries();
        final String baseFileName = "standard-directions-order.pdf";
        final String draftOrderFileName = "draft-standard-directions-order.pdf";

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument(baseFileName, pdf));

        given(uploadDocumentService.uploadPDF(pdf, draftOrderFileName)).willReturn(DOCUMENT);
    }

    @Test
    void shouldSetDraftDocumentAndNextStepsLabelWhenMandatoryInformationMissing() {
        String nextSteps = NEXT_STEPS
            + "* the first hearing details\n\n"
            + "* the allocated judge\n\n"
            + "* the judge issuing the order";

        CaseData caseData = buildBaseCaseData();

        GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
            .draftDocument(DOCUMENT_REFERENCE)
            .nextSteps(nextSteps)
            .dateOfIssue(LocalDate.now())
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "generate-draft"));

        assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision())
            .isEqualTo(expectedSealDecision);
    }

    @Test
    void shouldSetOnlyDraftDocumentWhenOrderCanBeSealed() {
        CaseData caseData = buildBaseCaseData().toBuilder()
            .allocatedJudge(allocatedJudge())
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderIssuingJudge(issuingJudge())
                .build())
            .hearingDetails(hearingBookings())
            .build();

        GatekeepingOrderSealDecision expectedSealDecision = GatekeepingOrderSealDecision.builder()
            .draftDocument(DOCUMENT_REFERENCE)
            .nextSteps(null)
            .dateOfIssue(LocalDate.now())
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "generate-draft"));

        assertThat(responseData.getGatekeepingOrderEventData().getGatekeepingOrderSealDecision())
            .isEqualTo(expectedSealDecision);
    }

    private CaseData buildBaseCaseData() {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(1234123412341234L)
            .dateSubmitted(dateNow())
            .applicants(getApplicant())
            .build();
    }

    private List<Element<Applicant>> getApplicant() {
        return wrapElements(Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("")
                .build())
            .build());
    }

    private List<Element<HearingBooking>> hearingBookings() {
        return wrapElements(HearingBooking.builder().startDate(LocalDateTime.now()).build());
    }

    private Judge allocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
            .judgeEmailAddress("davidson@example.com")
            .build();
    }

    private JudgeAndLegalAdvisor issuingJudge() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .judgeEmailAddress("judy@example.com")
            .build();
    }
}
