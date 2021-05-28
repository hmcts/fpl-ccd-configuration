package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.EPO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER;
import static uk.gov.hmcts.reform.fpl.enums.EnglandOffices.BOURNEMOUTH;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.WORD;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersAboutToSubmitControllerTest extends AbstractCallbackTest {

    private static final Child CHILD_1 = Child.builder()
        .party(ChildParty.builder().firstName("first1").lastName("last1").build())
        .build();
    private static final Child CHILD_2 = Child.builder()
        .party(ChildParty.builder().firstName("first2").lastName("last2").build())
        .build();
    private static final List<Element<Child>> CHILDREN = wrapElements(CHILD_1, CHILD_2);

    // need actual pdfs for the merging
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final byte[] POWER_OF_ARREST_BINARIES = readBytes("documents/document2.pdf");
    private static final byte[] DOCUMENT_WORD_BINARIES = testDocumentBinaries();
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
    private static final DocmosisDocument DOCMOSIS_WORD_DOCUMENT = testDocmosisDocument(DOCUMENT_WORD_BINARIES);
    private static final Document UPLOADED_PDF_DOCUMENT = testDocument();
    private static final Document UPLOADED_WORD_DOCUMENT = testDocument();
    private static final DocumentReference DOCUMENT_PDF_REFERENCE = buildFromDocument(UPLOADED_PDF_DOCUMENT);
    private static final DocumentReference DOCUMENT_WORD_REFERENCE = buildFromDocument(UPLOADED_WORD_DOCUMENT);
    private static final DocumentReference UPLOADED_POWER_OF_ARREST = testDocumentReference("PoA.pdf");

    private static final UUID ELEMENT_ID = UUID.randomUUID();

    @MockBean
    private DocmosisDocumentGeneratorService docmosisGenerationService;
    @MockBean
    private DocumentDownloadService downloadService;
    @MockBean
    private UploadDocumentService uploadService;
    @MockBean
    private IdentityService identityService;

    ManageOrdersAboutToSubmitControllerTest() {
        super("manage-orders");
    }

    @BeforeEach
    void mocks() {
        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(ORDER), eq(PDF)))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(EPO), eq(PDF)))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(downloadService.downloadDocument(UPLOADED_POWER_OF_ARREST.getBinaryUrl()))
            .thenReturn(POWER_OF_ARREST_BINARIES);
        // won't know merged document contents
        when(uploadService.uploadDocument(any(), eq("c23_emergency_protection_order.pdf"), eq("application/pdf")))
            .thenReturn(UPLOADED_PDF_DOCUMENT);
        when(uploadService.uploadDocument(eq(DOCUMENT_PDF_BINARIES), anyString(), eq("application/pdf")))
            .thenReturn(UPLOADED_PDF_DOCUMENT);

        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(ORDER), eq(WORD)))
            .thenReturn(DOCMOSIS_WORD_DOCUMENT);
        when(docmosisGenerationService.generateDocmosisDocument(anyMap(), eq(EPO), eq(WORD)))
            .thenReturn(DOCMOSIS_WORD_DOCUMENT);
        when(uploadService.uploadDocument(eq(DOCUMENT_WORD_BINARIES), anyString(), eq("application/msword")))
            .thenReturn(UPLOADED_WORD_DOCUMENT);

        when(identityService.generateId()).thenReturn(ELEMENT_ID);
    }

    @Test
    void shouldBuildNewOrderObject() {
        CaseData caseData = buildCaseData().toBuilder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(C32_CARE_ORDER)
                .manageOrdersApprovalDate(dateNow())
                .manageOrdersFurtherDirections("Some further directions")
                .build())
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getOrderCollection()).containsOnly(
            element(ELEMENT_ID, GeneratedOrder.builder()
                .orderType("C32_CARE_ORDER")
                .type("C32 - Care order")
                .children(CHILDREN)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now())
                .approvalDate(dateNow())
                .childrenDescription("first1 last1, first2 last2")
                .document(DOCUMENT_PDF_REFERENCE)
                .unsealedDocumentCopy(DOCUMENT_WORD_REFERENCE)
                .build())
        );
    }

    @Test
    void shouldBuildNewEPO() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(now())
            .manageOrdersEndDateTime(now().plusDays(1))
            .manageOrdersType(C23_EMERGENCY_PROTECTION_ORDER)
            .manageOrdersEpoType(EPOType.PREVENT_REMOVAL)
            .manageOrdersEpoRemovalAddress(Address.builder().addressLine1("address1").postcode("postcode").build())
            .manageOrdersChildrenDescription("first1 last1")
            .manageOrdersFurtherDirections("test directions")
            .manageOrdersExclusionRequirement("Yes")
            .manageOrdersWhoIsExcluded("John")
            .manageOrdersExclusionStartDate(dateNow().plusDays(2))
            .manageOrdersPowerOfArrest(UPLOADED_POWER_OF_ARREST)
            .build();

        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(manageOrdersEventData).build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getOrderCollection()).containsOnly(
            element(ELEMENT_ID, GeneratedOrder.builder()
                .orderType("C23_EMERGENCY_PROTECTION_ORDER")
                .type("C23 - Emergency protection order")
                .children(CHILDREN)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now())
                .approvalDateTime(manageOrdersEventData.getManageOrdersApprovalDateTime())
                .childrenDescription("first1 last1, first2 last2")
                .document(DOCUMENT_PDF_REFERENCE)
                .unsealedDocumentCopy(DOCUMENT_WORD_REFERENCE)
                .build()));
    }

    @Test
    void shouldBuildNewBlankOrderForClosedCase() {
        CaseData caseData = buildCaseData().toBuilder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDate(dateNow())
                .manageOrdersDirections("Some directions")
                .build())
            .build();

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(responseCaseData.getOrderCollection()).containsOnly(
            element(ELEMENT_ID, GeneratedOrder.builder()
                .orderType("C21_BLANK_ORDER")
                .type("C21 - Blank order")
                .children(CHILDREN)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now())
                .approvalDate(dateNow())
                .childrenDescription("first1 last1, first2 last2")
                .document(DOCUMENT_PDF_REFERENCE)
                .unsealedDocumentCopy(DOCUMENT_WORD_REFERENCE)
                .build())
        );
    }

    @Test
    void shouldRemoveTransientFields() {
        CaseData caseData = buildCaseData().toBuilder().manageOrdersEventData(
            ManageOrdersEventData.builder()
                .manageOrdersType(C23_EMERGENCY_PROTECTION_ORDER)
                .manageOrdersEpoType(EPOType.REMOVE_TO_ACCOMMODATION)
                .manageOrdersApprovalDateTime(now())
                .manageOrdersEndDateTime(now().plusDays(1))
                .manageOrdersPowerOfArrest(UPLOADED_POWER_OF_ARREST)
                .manageOrdersFurtherDirections("further directions")
                .manageOrdersCafcassRegion("ENGLAND")
                .manageOrdersCafcassOfficesEngland(BOURNEMOUTH).build())
            .build();

        CaseDetails caseDetails = asCaseDetails(caseData);

        // dummy data set for the front end that is dirtying case data
        caseDetails.getData().putAll(Map.of(
            "manageOrdersOperation", "CREATE",
            "orderTempQuestions", Map.of("holderObject", "forQuestionConditions"),
            "hearingDetailsSectionSubHeader", "some heading",
            "issuingDetailsSectionSubHeader", "some heading",
            "childrenDetailsSectionSubHeader", "some heading",
            "children_label", "some label about the children",
            "orderDetailsSectionSubHeader", "some heading",
            "orderPreview", DOCUMENT_PDF_REFERENCE
        ));

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "judgeAndLegalAdvisor", "manageOrdersApprovalDate", "orderAppliesToAllChildren", "children_label",
            "childSelector", "manageOrdersFurtherDirections", "orderPreview", "manageOrdersType", "orderTempQuestions",
            "issuingDetailsSectionSubHeader", "hearingDetailsSectionSubHeader",
            "childrenDetailsSectionSubHeader", "orderDetailsSectionSubHeader",
            "manageOrdersOperation", "manageOrdersApprovalDateTime", "manageOrdersIncludePhrase",
            "manageOrdersChildrenDescription", "manageOrdersEndDateTime", "manageOrdersEpoType",
            "manageOrdersEpoRemovalAddress", "manageOrdersExclusionRequirement", "manageOrdersWhoIsExcluded",
            "manageOrdersExclusionStartDate", "manageOrdersPowerOfArrest", "manageOrdersTitle",
            "manageOrdersDirections", "manageOrdersCafcassOfficesEngland", "manageOrdersCafcassRegion"
        );
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(1234123412341234L)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber("CASE_NUMBER")
            .children1(CHILDREN)
            .orderAppliesToAllChildren("Yes")
            .childSelector(Selector.newSelector(2))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().useAllocatedJudge("Yes").build())
            .allocatedJudge(Judge.builder().judgeLastName("Dredd").judgeTitle(HIS_HONOUR_JUDGE).build())
            .build();
    }
}
