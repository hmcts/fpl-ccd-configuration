package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final String C6_DOCUMENT_TITLE = C6.getDocumentTitle();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    @Autowired
    private NoticeOfProceedingsService noticeOfProceedingsService;
    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    @MockBean
    private NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;
    @MockBean
    private UploadDocumentService uploadDocumentService;

    NoticeOfProceedingsControllerAboutToSubmitTest() {
        super("notice-of-proceedings");
    }

    @Test
    void shouldGenerateC6NoticeOfProceedingsDocument() {
        Document document = document();
        DocmosisDocument docmosisDocument = DocmosisDocument.builder()
            .bytes(PDF)
            .documentTitle(C6_DOCUMENT_TITLE)
            .build();

        CaseData caseData = caseConverter.convert(callbackRequest().getCaseDetails());

        DocmosisNoticeOfProceeding templateData = createTemplatePlaceholders();

        given(noticeOfProceedingsTemplateDataGenerationService.getTemplateData(caseData))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(PDF, C6_DOCUMENT_TITLE))
            .willReturn(document);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1);

        DocumentReference noticeOfProceedingBundle = responseCaseData.getNoticeOfProceedingsBundle().get(0).getValue()
            .getDocument();

        assertThat(noticeOfProceedingBundle.getUrl()).isEqualTo(document.links.self.href);
        assertThat(noticeOfProceedingBundle.getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(noticeOfProceedingBundle.getBinaryUrl()).isEqualTo(document.links.binary.href);
    }

    private DocmosisNoticeOfProceeding createTemplatePlaceholders() {

        return DocmosisNoticeOfProceeding.builder()
            .courtName("Swansea Family Court")
            .familyManCaseNumber("SW123123")
            .applicantName("James Nelson")
            .orderTypes("Care order")
            .childrenNames("James Nelson")
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate("1 Jan 2001")
                .hearingVenue("Aldgate Tower floor 3")
                .preHearingAttendance("test")
                .hearingTime("09.00pm")
                .build())
            .build();
    }
}
