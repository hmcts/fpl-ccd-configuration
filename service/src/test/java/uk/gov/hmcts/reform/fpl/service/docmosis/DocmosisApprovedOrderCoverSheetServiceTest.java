package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApprovedOrderCoverSheet;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPROVED_ORDER_COVER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
    public static final String COURT_NAME = "Test Court";
    public static final String JUDGE_NAME = "Test Judge";

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    @Mock
    private CaseDataExtractionService caseDataExtractionService;
    @Mock
    private JudicialService judicialService;
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
            .build();

        given(caseDataExtractionService.getCourtName(caseData)).willReturn(COURT_NAME);
        given(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren())).willReturn(DOCMOSIS_CHILDREN);
        given(judicialService.getJudgeTitleAndNameOfCurrentUser()).willReturn(JUDGE_NAME);
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
}
