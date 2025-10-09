package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApprovedOrderCoverSheet;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPROVED_ORDER_COVER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class DocmosisApprovedOrderCoverSheetServiceTest {
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025,3,26,8,0,0,0);
    private static final String FAMILY_MAN_NUMBER = "FMN-001";
    private static final Long CASE_ID = 1L;
    private static final Language LANGUAGE = Language.ENGLISH;
    private static final Court COURT = Court.builder().code("999").build();
    private static final byte[] PDF_BYTES = {10, 20, 30, 40, 50};
    private static final String FILE_NAME = "approved-order-cover.pdf";
    private static final Element<Child> CHILD = element(Child.builder().party(ChildParty.builder()
        .firstName("Test").lastName("Child").build()).build());
    private static final Element<Child> CONFIDENTIAL_CHILD = element(Child.builder().party(ChildParty.builder()
        .firstName("Confidential").lastName("Child").build()).build());
    private static final List<DocmosisChild> DOCMOSIS_CHILDREN = List.of(
        DocmosisChild.builder().name("Test Child").build(),
        DocmosisChild.builder().name("Confidential Child").build()
    );
    private static final DocmosisDocument COVER_SHEET = new DocmosisDocument(FILE_NAME, PDF_BYTES);
    private static final String COURT_NAME = "Test Court";
    private static final String JUDGE_NAME = "Test Judge";

    private static final Element<DraftOrder> DRAFT_ORDER =
        element(DraftOrder.builder().document(testDocumentReference()).build());
    private static final Element<AdditionalApplicationsBundle> C2_BUNDLE =
        element(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                .type(C2ApplicationType.WITH_NOTICE)
                .draftOrdersBundle(List.of(DRAFT_ORDER)).build())
            .build());
    private static final Element<HearingOrder> HEARING_ORDER =
        element(DRAFT_ORDER.getId(), HearingOrder.from(DRAFT_ORDER.getValue()));

    private static final Element<DraftOrder> DRAFT_ORDER_WITH_CONSENT =
        element(DraftOrder.builder().document(testDocumentReference()).build());
    private static final Element<AdditionalApplicationsBundle> C2_BUNDLE_WITH_CONSENT =
        element(AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                .type(C2ApplicationType.WITHOUT_NOTICE)
                .draftOrdersBundle(List.of(DRAFT_ORDER_WITH_CONSENT)).build())
            .build());
    private static final Element<HearingOrder> HEARING_ORDER_WITH_CONSENT =
        element(DRAFT_ORDER_WITH_CONSENT.getId(), HearingOrder.from(DRAFT_ORDER_WITH_CONSENT.getValue()));


    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    @Mock
    private CaseDataExtractionService caseDataExtractionService;
    @Mock
    private DocumentMerger documentMerger;
    @Mock
    private Time time;
    @InjectMocks
    private DocmosisApprovedOrderCoverSheetService underTest;

    @Test
    void shouldGenerateApprovedOrderCoverSheet() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .court(COURT)
            .c110A(C110A.builder()
                .languageRequirementApplication(LANGUAGE)
                .build())
            .children1(List.of(CHILD))
            .confidentialChildren(List.of(CONFIDENTIAL_CHILD))
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().judgeTitleAndName(JUDGE_NAME).build())
            .additionalApplicationsBundle(List.of(C2_BUNDLE))
            .build();

        given(caseDataExtractionService.getCourtName(caseData)).willReturn(COURT_NAME);
        given(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren())).willReturn(DOCMOSIS_CHILDREN);
        given(time.now()).willReturn(TEST_TIME);

        DocmosisApprovedOrderCoverSheet expectedDocmosisData = DocmosisApprovedOrderCoverSheet.builder()
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .courtName(COURT_NAME)
            .children(DOCMOSIS_CHILDREN)
            .judgeTitleAndName(JUDGE_NAME)
            .dateOfApproval(formatLocalDateToString(TEST_TIME.toLocalDate(), DATE, Language.ENGLISH))
            .crest(CREST.getValue())
            .build();


        given(docmosisDocumentGeneratorService.generateDocmosisDocument(expectedDocmosisData, APPROVED_ORDER_COVER,
                RenderFormat.PDF, LANGUAGE))
            .willReturn(COVER_SHEET);

        DocmosisDocument result = underTest.createCoverSheet(caseData);

        assertThat(result).isEqualTo(COVER_SHEET);
    }

    @Test
    void shouldAddCoverSheetAndAddAnnexAWording() throws IOException {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .court(COURT)
            .c110A(C110A.builder()
                .languageRequirementApplication(LANGUAGE)
                .build())
            .children1(List.of(CHILD))
            .confidentialChildren(List.of(CONFIDENTIAL_CHILD))
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().judgeTitleAndName(JUDGE_NAME).build())
            .build();

        given(caseDataExtractionService.getCourtName(caseData)).willReturn(COURT_NAME);
        given(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren())).willReturn(DOCMOSIS_CHILDREN);
        given(time.now()).willReturn(TEST_TIME);

        DocmosisApprovedOrderCoverSheet expectedDocmosisData = DocmosisApprovedOrderCoverSheet.builder()
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .courtName(COURT_NAME)
            .children(DOCMOSIS_CHILDREN)
            .judgeTitleAndName(JUDGE_NAME)
            .dateOfApproval(formatLocalDateToString(TEST_TIME.toLocalDate(), DATE, Language.ENGLISH))
            .crest(CREST.getValue())
            .build();


        given(docmosisDocumentGeneratorService.generateDocmosisDocument(expectedDocmosisData, APPROVED_ORDER_COVER,
            RenderFormat.PDF, LANGUAGE))
            .willReturn(COVER_SHEET);

        byte[] mergedOrderBytes = ResourceReader.readBytes("documents/document.pdf");
        DocmosisDocument mergedOrder = new DocmosisDocument("merged_order.pdf", mergedOrderBytes);
        given(documentMerger.mergeDocuments(any(), any())).willReturn(mergedOrder);

        DocmosisDocument result = underTest.addCoverSheetToApprovedOrder(caseData, testDocumentReference());

        String resultText = (new DocmosisHelper()).extractPdfContent(result.getBytes());

        assertThat(resultText).isEqualToNormalizingWhitespace("First page Second page ANNEX A: ");
    }
}
