package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersMidEventControllerTest extends AbstractCallbackTest {

    private static final String FAMILY_MAN_CASE_NUMBER = "CASE_NUMBER";

    private static final Judge JUDGE = Judge.builder()
        .judgeLastName("Judy")
        .judgeTitle(DISTRICT_JUDGE)
        .build();

    private static final Child CHILD_1 = Child.builder()
        .party(ChildParty.builder().firstName("first1").lastName("last1").build())
        .build();
    private static final Child CHILD_2 = Child.builder()
        .party(ChildParty.builder().firstName("first2").lastName("last2").build())
        .build();
    private static final List<Element<Child>> CHILDREN = wrapElements(CHILD_1, CHILD_2);

    private static final byte[] DOCUMENT_BINARIES = testDocumentBinaries();
    private static final DocmosisDocument DOCMOSIS_DOCUMENT = testDocmosisDocument(DOCUMENT_BINARIES);
    private static final Document UPLOADED_DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_REFERENCE = buildFromDocument(UPLOADED_DOCUMENT);

    @MockBean
    private DocmosisDocumentGeneratorService docmosisGenerationService;

    @MockBean
    private UploadDocumentService uploadService;

    ManageOrdersMidEventControllerTest() {
        super("manage-orders");
    }

    @Test
    void orderSelectionShouldPopulateQuestionConditionHolder() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-selection");

        assertThat(response.getData().get("orderTempQuestions")).isEqualTo(
            Map.of(
                "approvalDate", "YES",
                "approver", "YES",
                "previewOrder", "YES",
                "furtherDirections", "YES",
                "whichChildren", "YES"
            )
        );
    }

    @Test
    void orderSelectionShouldPrePopulateFirstSectionDetails() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .allocatedJudge(JUDGE)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-selection");

        CaseData responseCaseData = extractCaseData(response);

        assertThat(response.getData().get("issuingDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
        assertThat(responseCaseData.getJudgeAndLegalAdvisor()).isEqualTo(
            JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: District Judge Judy")
                .build()
        );
    }

    @Test
    void issuingDetailsShouldValidateAgainstFutureApprovalDate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow().plusDays(1))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "issuing-details");

        assertThat(response.getErrors()).containsOnly("Approval date cannot not be in the future");
    }

    @Test
    void issuingDetailsShouldPrepopulateNextSectionDetails() {
        final CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "issuing-details");

        CaseData responseCaseData = extractCaseData(response);

        assertThat(responseCaseData.getChildSelector()).isEqualTo(Selector.newSelector(2));
        assertThat(response.getData().get("children_label"))
            .isEqualTo("Child 1: first1 last1\nChild 2: first2 last2\n");

        assertThat(response.getData().get("childrenDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
    }

    @Test
    void childrenDetailsShouldReturnErrorWhenNoChildrenSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .childSelector(Selector.newSelector(2))
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "children-details");

        assertThat(response.getErrors()).containsOnly("Select the children included in the order");
    }

    @Test
    void childrenDetailsShouldPrepopulateNextSectionDetails() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("Yes")
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "children-details");

        assertThat(response.getData().get("orderDetailsSectionSubHeader")).isEqualTo("C32 - Care order");
    }

    @Test
    void orderDetailsShouldPrepopulateNextSectionDetails() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(CHILDREN)
            .orderAppliesToAllChildren("Yes")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
            .allocatedJudge(JUDGE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .build())
            .build();

        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(ORDER), eq(PDF)))
            .thenReturn(DOCMOSIS_DOCUMENT);
        when(uploadService.uploadDocument(DOCUMENT_BINARIES, "Preview order.pdf", "application/pdf"))
            .thenReturn(UPLOADED_DOCUMENT);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "order-details");

        Map<String, String> mappedDocument = mapper.convertValue(DOCUMENT_REFERENCE, new TypeReference<>() {});

        assertThat(response.getData().get("orderPreview")).isEqualTo(mappedDocument);
    }

    @Test
    void reviewShouldNotAlterCaseData() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(C32_CARE_ORDER).build())
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "review");

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void shouldThrowExceptionWhenMidEventUrlParameterDoesNotMatchSectionNames() {
        assertThatThrownBy(() -> postMidEvent(CaseData.builder().build(), "does-not-match"))
            .getRootCause()
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.model.order.OrderSection.DOES_NOT_MATCH");
    }
}
