package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private FeeService feeService;

    @MockBean
    private CaseSubmissionService caseSubmissionService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private final Document document = document();

    CaseSubmissionControllerAboutToStartTest() {
        super("case-submission");
    }

    @BeforeEach
    void mocking() {
        givenCurrentUserWithName("Emma Taylor");
        given(caseSubmissionService.generateSubmittedFormPDF(any(), eq(true)))
            .willReturn(document);
        given(uploadDocumentService.uploadPDF(DOCUMENT_CONTENT, "2313.pdf"))
            .willReturn(document);
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(CaseData.builder()
            .caseName("title")
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("submissionConsentLabel",
                "I, Emma Taylor, believe that the facts stated in this application are true.");
    }

    @Test
    void shouldAddConsentLabelToCaseDetailsWhenLegalTeamManagerPresent() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(CaseData.builder()
            .caseName("title")
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .legalTeamManager("legal team manager")
                    .build())
                .build()))
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("submissionConsentLabel",
                "I, legal team manager, believe that the facts stated in this application are true.");
    }

    @Test
    void shouldAddAmountToPayFieldToAnOpenedCase() {
        Orders orders = Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build();
        FeesData feesData = FeesData.builder()
            .totalAmount(BigDecimal.valueOf(123))
            .build();

        given(feeService.getFeesDataForOrders(orders)).willReturn(feesData);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(of("orders", orders))
            .state(OPEN.getValue())
            .build());

        assertThat(response.getData()).containsEntry("amountToPay", "12300");
        assertThat(response.getData()).containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldNotDisplayAmountToPayFieldToAnOpenedCaseWhenErrorIsThrown() {
        given(feeService.getFeesDataForOrders(any())).willThrow(new FeeRegisterException(300, "duplicate", null));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(of())
            .state(OPEN.getValue())
            .build());

        assertThat(response.getData()).doesNotContainKey("amountToPay");
        assertThat(response.getData()).containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldNotDisplayAmountToPayFieldWhenCaseIsInReturnedState() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(of())
            .state(RETURNED.getValue())
            .build());

        verify(feeService, never()).getFeesDataForOrders(any());
        assertThat(response.getData()).doesNotContainKeys("amountToPay", "displayAmountToPay");
    }

    @Test
    void shouldHaveDraftApplicationDocumentInResponse() {
        given(feeService.getFeesDataForOrders(any())).willThrow(new FeeRegisterException(300, "duplicate", null));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(populatedCaseDetails());

        assertThat(callbackResponse.getData())
            .containsEntry("draftApplicationDocument",
                of("document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                    "document_filename", "file.pdf",
                    "document_binary_url",
                    "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary"));
    }
}
