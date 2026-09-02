package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationRefusalOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.REFUSAL_ORDER;
import static uk.gov.hmcts.reform.fpl.model.document.SealType.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;

@ExtendWith(MockitoExtension.class)
public class ApplicationRefusalOrderServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 28, 10, 15);
    private static final byte[] DOCMOSIS_BYTES = testDocumentBinary();
    private static final byte[] SEALED_BYTES = testDocumentBinary();

    private static final List<DocmosisChild> DOCMOSIS_CHILDREN =
        List.of(DocmosisChild.builder().name("Child One").age("90").build());
    private static final Document REFUSAL_ORDER_DOC = testDocument();
    private static final String COURT_NAME = "Test Court Name";
    private static final String JUDGE_TITLE_AND_NAME = "District Judge Example";
    private static final String DATE_OF_REFUSAL = "2 January 2026";
    private static final String APPLICATION_DATE = "1 January 2026";
    private static final String REFUSAL_REASON = "Invalid application";

    @Mock
    private CaseDataExtractionService caseDataExtractionService;

    @Mock
    private Time time;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService documentUploadService;

    @Mock
    private DocumentSealingService documentSealingService;

    @Captor
    private ArgumentCaptor<DocmosisApplicationRefusalOrder> docmosisTemplateDataCaptor;

    @InjectMocks
    private ApplicationRefusalOrderService underTest;

    @BeforeEach
    void setUp() {
        CaseData caseData = getCaseData();

        when(caseDataExtractionService.getCourtName(caseData)).thenReturn(COURT_NAME);
        when(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren())).thenReturn(DOCMOSIS_CHILDREN);
        when(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisTemplateDataCaptor.capture(),
            eq(DocmosisTemplates.APPLICATION_REFUSAL_ORDER)))
            .thenReturn(DocmosisDocument.builder()
                .bytes(DOCMOSIS_BYTES)
                .documentTitle(DocmosisTemplates.APPLICATION_REFUSAL_ORDER.getDocumentTitle())
                .build());
    }

    @Test
    void shouldBuildUnsealedApplicationRefusalOrderDocument() {
        CaseData caseData = getCaseData();

        when(documentUploadService.uploadPDF(DOCMOSIS_BYTES, REFUSAL_ORDER.getFileName()))
            .thenReturn(REFUSAL_ORDER_DOC);

        DocumentReference documentReference = underTest.buildApplicationRefusalOrderDocument(
            caseData, JUDGE_TITLE_AND_NAME, DATE_OF_REFUSAL, APPLICATION_DATE, REFUSAL_REASON, false);

        DocmosisApplicationRefusalOrder templateData = docmosisTemplateDataCaptor.getValue();
        assertThat(templateData).isEqualTo(DocmosisApplicationRefusalOrder.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(COURT_NAME)
            .children(DOCMOSIS_CHILDREN)
            .judgeTitleAndName(JUDGE_TITLE_AND_NAME)
            .dateOfRefusal(DATE_OF_REFUSAL)
            .applicationDate(APPLICATION_DATE)
            .refusalReason(REFUSAL_REASON)
            .crest(CREST.getValue())
            .build());

        assertThat(documentReference).isEqualTo(DocumentReference.buildFromDocument(REFUSAL_ORDER_DOC));
        verifyNoInteractions(documentSealingService);
    }

    @Test
    void shouldSealApplicationRefusalOrderDocumentWhenRequired() {
        CaseData caseData = getCaseData();

        when(documentSealingService.sealDocument(DOCMOSIS_BYTES, caseData.getCourt(), ENGLISH))
            .thenReturn(SEALED_BYTES);
        when(documentUploadService.uploadPDF(SEALED_BYTES, REFUSAL_ORDER.getFileName()))
            .thenReturn(REFUSAL_ORDER_DOC);

        DocumentReference documentReference = underTest.buildApplicationRefusalOrderDocument(
            caseData, JUDGE_TITLE_AND_NAME, DATE_OF_REFUSAL, APPLICATION_DATE, REFUSAL_REASON, true);

        DocmosisApplicationRefusalOrder templateData = docmosisTemplateDataCaptor.getValue();
        assertThat(templateData).isEqualTo(DocmosisApplicationRefusalOrder.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(COURT_NAME)
            .children(DOCMOSIS_CHILDREN)
            .judgeTitleAndName(JUDGE_TITLE_AND_NAME)
            .dateOfRefusal(DATE_OF_REFUSAL)
            .applicationDate(APPLICATION_DATE)
            .refusalReason(REFUSAL_REASON)
            .crest(CREST.getValue())
            .build());

        assertThat(documentReference).isEqualTo(DocumentReference.buildFromDocument(REFUSAL_ORDER_DOC));
    }

    @Test
    void shouldUseCurrentDateWhenBuildingApplicationRefusalOrderWithoutExplicitRefusalDate() {
        CaseData caseData = getCaseData();

        when(time.now()).thenReturn(NOW);
        when(documentUploadService.uploadPDF(DOCMOSIS_BYTES, REFUSAL_ORDER.getFileName()))
            .thenReturn(REFUSAL_ORDER_DOC);

        underTest.buildApplicationRefusalOrderDocument(
            caseData,
            JUDGE_TITLE_AND_NAME,
            APPLICATION_DATE,
            REFUSAL_REASON,
            false
        );

        String expectedDateOfRefusal = formatLocalDateToString(NOW.toLocalDate(), DATE, caseData.getCaseLanguage());
        assertThat(docmosisTemplateDataCaptor.getValue().getDateOfRefusal()).isEqualTo(expectedDateOfRefusal);
    }

    @Test
    void shouldBuildRefusalOrder() {
        CaseData caseData = getCaseData();

        when(time.now()).thenReturn(NOW);
        when(documentSealingService.sealDocument(DOCMOSIS_BYTES, caseData.getCourt(), ENGLISH))
            .thenReturn(SEALED_BYTES);
        when(documentUploadService.uploadPDF(SEALED_BYTES, REFUSAL_ORDER.getFileName()))
            .thenReturn(REFUSAL_ORDER_DOC);

        Element<GeneratedOrder> refusalOrder = underTest.buildRefusalOrder(
            caseData,
            JUDGE_TITLE_AND_NAME,
            DATE_OF_REFUSAL,
            APPLICATION_DATE,
            REFUSAL_REASON
        );

        GeneratedOrder order = refusalOrder.getValue();
        assertThat(order).isEqualTo(GeneratedOrder.builder()
            .type(REFUSAL_ORDER.getLabel())
            .title(format("%s for application date %s", REFUSAL_ORDER.getLabel(), APPLICATION_DATE))
            .dateOfIssue(DATE_OF_REFUSAL)
            .judgeAndLegalAdvisor(null)
            .date(formatLocalDateTimeBaseUsingFormat(NOW, TIME_DATE))
            .children(caseData.getAllChildren())
            .refusalDocument(DocumentReference.buildFromDocument(REFUSAL_ORDER_DOC))
            .build());
        verify(documentSealingService).sealDocument(DOCMOSIS_BYTES, caseData.getCourt(), ENGLISH);
    }

    @Test
    void shouldUseCurrentDateWhenBuildingRefusalOrderWithoutExplicitRefusalDate() {
        CaseData caseData = getCaseData();

        when(time.now()).thenReturn(NOW);
        when(documentSealingService.sealDocument(DOCMOSIS_BYTES, caseData.getCourt(), ENGLISH))
            .thenReturn(SEALED_BYTES);
        when(documentUploadService.uploadPDF(SEALED_BYTES, REFUSAL_ORDER.getFileName()))
            .thenReturn(REFUSAL_ORDER_DOC);

        Element<GeneratedOrder> refusalOrder = underTest.buildRefusalOrder(
            caseData,
            JUDGE_TITLE_AND_NAME,
            APPLICATION_DATE,
            REFUSAL_REASON
        );

        String expectedDateOfRefusal = formatLocalDateToString(NOW.toLocalDate(), DATE, caseData.getCaseLanguage());
        assertThat(refusalOrder.getValue().getDateOfIssue()).isEqualTo(expectedDateOfRefusal);
    }

    private CaseData getCaseData() {
        return CaseData.builder()
            .familyManCaseNumber("1234567890")
            .court(Court.builder().name("Test court").build())
            .build();
    }
}
