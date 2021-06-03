package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class AddGatekeepingOrderControllerAboutToSubmitTest extends AbstractCallbackTest {
    AddGatekeepingOrderControllerAboutToSubmitTest() {
        super("add-gatekeeping-order");
    }

    private static final Document DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = DocumentReference.buildFromDocument(DOCUMENT);

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 3, 0, 0, 0));

    @BeforeEach
    void setup() {
        final byte[] pdf = testDocumentBinaries();
        final String sealedOrderFileName = "standard-directions-order.pdf";

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument(sealedOrderFileName, pdf));

        given(uploadDocumentService.uploadPDF(pdf, sealedOrderFileName)).willReturn(DOCUMENT);
    }

    @Test
    void shouldBuildDraftSDOWithExistingDraftDocumentWhenOrderStatusIsDraft() {
        CaseData caseData = CaseData.builder()
            .saveOrSendGatekeepingOrder(SaveOrSendGatekeepingOrder.builder()
                .orderStatus(DRAFT)
                .draftDocument(DOCUMENT_REFERENCE)
                .build())

            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(DOCUMENT_REFERENCE)
            .orderStatus(DRAFT)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseData.getStandardDirectionOrder()).isEqualTo(expectedSDO);
    }

    @Test
    void shouldBuildSealedSDOAndRemoveTransientFieldsWhenOrderStatusIsSealed() {
        List<Element<CustomDirection>> customDirections = wrapElements(
            CustomDirection.builder().title("Test direction").build());

        CaseData caseData = buildBaseCaseData().toBuilder()
            .gatekeepingOrderIssuingJudge(JudgeAndLegalAdvisor.builder().build())
            .sdoDirectionCustom(customDirections)
            .saveOrSendGatekeepingOrder(SaveOrSendGatekeepingOrder.builder()
                .orderStatus(SEALED)
                .dateOfIssue(time.now().toLocalDate())
                .build())
            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderDoc(DOCUMENT_REFERENCE)
            .orderStatus(SEALED)
            .dateOfIssue("3 March 2021")
            .customDirections(customDirections)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        StandardDirectionOrder responseSDO = extractCaseData(response).getStandardDirectionOrder();

        assertThat(responseSDO).isEqualTo(expectedSDO);
        assertThat(response.getData().get("state")).isEqualTo("PREPARE_FOR_HEARING");
        assertThat(response.getData()).doesNotContainKeys("gatekeepingOrderRouter", "sdoDirectionCustom",
            "gatekeepingOrderIssuingJudge", "saveOrSendGatekeepingOrder");
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
}
