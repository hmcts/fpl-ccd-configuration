package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final String C6_DOCUMENT_TITLE = C6.getDocumentTitle();
    private static final byte[] PDF = {1, 2, 3, 4, 5};
    @MockBean
    private NoticeOfProceedingsService noticeOfProceedingsService;
    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
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

        CaseData caseData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        Map<String, Object> templateData = createTemplatePlaceholders();

        given(noticeOfProceedingsService.getNoticeOfProceedingTemplateData(caseData))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(PDF, C6_DOCUMENT_TITLE))
            .willReturn(document);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest());

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1);

        DocumentReference noticeOfProceedingBundle = responseCaseData.getNoticeOfProceedingsBundle().get(0).getValue()
            .getDocument();

        assertThat(noticeOfProceedingBundle.getUrl()).isEqualTo(document.links.self.href);
        assertThat(noticeOfProceedingBundle.getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(noticeOfProceedingBundle.getBinaryUrl()).isEqualTo(document.links.binary.href);
    }

    @Test
    void shouldMigrateJudgeAndLegalAdvisorWhenUsingAllocatedJudge() {
        Document document = document();
        DocmosisDocument docmosisDocument = DocmosisDocument.builder()
            .bytes(PDF)
            .documentTitle(C6_DOCUMENT_TITLE)
            .build();

        CallbackRequest callbackRequest = buildCallbackRequest();

        Map<String, Object> templateData = createTemplatePlaceholders();

        given(noticeOfProceedingsService.getNoticeOfProceedingTemplateData(any()))
            .willReturn(templateData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(anyMap(), any()))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(any(), any()))
            .willReturn(document);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest);

        CaseData responseCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = responseCaseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor).isNotNull();
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HER_HONOUR_JUDGE);
        assertThat(judgeAndLegalAdvisor.getJudgeFullName()).isEqualTo("Davidson");
    }

    private CallbackRequest buildCallbackRequest() {
        CallbackRequest callbackRequest = callbackRequest();

        callbackRequest.getCaseDetails().getData().put("noticeOfProceedings", NoticeOfProceedings.builder()
            .proceedingTypes(ImmutableList.of(NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .useAllocatedJudge("Yes")
                .build())
            .build());

        callbackRequest.getCaseDetails().getData().put("allocatedJudge", Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeFullName("Davidson")
            .build());

        return callbackRequest;
    }

    private Map<String, Object> createTemplatePlaceholders() {
        return Map.of(
            "courtName", "Swansea Family Court",
            "familyManCaseNumber", "SW123123",
            "applicantName", "James Nelson",
            "orderTypes", "Care order",
            "childrenNames", "James Nelson",
            "hearingDate", "1 Jan 2001",
            "hearingVenue", "Aldgate Tower floor 3",
            "preHearingAttendance", "test",
            "hearingTime", "09.00pm"
        );
    }
}
