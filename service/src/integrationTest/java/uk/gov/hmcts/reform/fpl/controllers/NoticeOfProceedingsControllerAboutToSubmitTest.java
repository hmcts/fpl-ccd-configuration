package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;

@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final byte[] PDF = testDocumentBinaries();
    private static final LocalDateTime NOW = LocalDateTime.now();

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
            .documentTitle(C6.getDocumentTitle())
            .build();

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(any(DocmosisNoticeOfProceeding.class), eq(C6)))
            .willReturn(docmosisDocument);

        given(uploadDocumentService.uploadPDF(PDF, C6.getDocumentTitle()))
            .willReturn(document);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(buildCaseData()));

        assertThat(responseCaseData.getNoticeOfProceedingsBundle()).hasSize(1);

        DocumentReference noticeOfProceedingBundle = responseCaseData.getNoticeOfProceedingsBundle().get(0).getValue()
            .getDocument();

        assertThat(noticeOfProceedingBundle.getUrl()).isEqualTo(document.links.self.href);
        assertThat(noticeOfProceedingBundle.getFilename()).isEqualTo(document.originalDocumentName);
        assertThat(noticeOfProceedingBundle.getBinaryUrl()).isEqualTo(document.links.binary.href);
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .familyManCaseNumber("SW123123")
            .id(1234123412341234L)
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .proceedingTypes(List.of(NOTICE_OF_PROCEEDINGS_FOR_PARTIES))
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeLastName("Wilson")
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .build())
                .build())
            .hearingDetails(List.of(ElementUtils.element(HearingBooking.builder()
                .startDate(NOW.plusDays(1))
                .endDate(NOW.plusDays(2))
                .venue("Some venue")
                .build())))
            .orders(Orders.builder()
                .orderType(List.of(CARE_ORDER))
                .build())
            .build();
    }
}
