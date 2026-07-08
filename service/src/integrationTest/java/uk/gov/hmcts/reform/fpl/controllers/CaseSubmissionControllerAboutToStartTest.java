package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
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
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
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
        given(caseSubmissionService.generateC110aSubmittedFormPDF(any(), eq(true)))
                .willReturn(document);
        given(caseSubmissionService.generateC1SubmittedFormPDF(any(), eq(true)))
                .willReturn(document);
        given(caseSubmissionService.generateC1SupplementPDF(any(), eq(true)))
                .willReturn(document);
        given(uploadDocumentService.uploadPDF(DOCUMENT_CONTENT, "2313.pdf"))
            .willReturn(document);
        given(caseSubmissionService.getSigneeName(any())).willReturn("Emma Taylor");
        given(caseSubmissionService.generateCaseName(any())).willReturn("LA & Respondent 1, Etc, Etc");
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() {
        given(feeService.getFeesDataForOrders(any())).willReturn(feesData(10));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData().toBuilder()
            .caseName("title")
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "LA & Respondent 1, Etc, Etc")
            .containsEntry("submissionConsentLabel",
                "I, Emma Taylor, believe that the facts stated in this application are true.");
    }

    @Test
    void shouldAddAmountToPayFieldToAnOpenedCase() {
        given(feeService.getFeesDataForOrders(any())).willReturn(feesData(123));

        Orders orders = Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData().toBuilder()
            .orders(orders)
            .state(OPEN)
            .build());

        assertThat(response.getData()).containsEntry("amountToPay", "12300");
        assertThat(response.getData()).containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldNotDisplayAmountToPayFieldToAnOpenedCaseWhenErrorIsThrown() {
        given(feeService.getFeesDataForOrders(any())).willThrow(new FeeRegisterException(300, "duplicate", null));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData().toBuilder()
            .state(OPEN)
            .build());

        assertThat(response.getData()).containsEntry("amountToPay", null);
        assertThat(response.getData()).containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldNotDisplayAmountToPayFieldWhenCaseIsInReturnedState() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData().toBuilder()
            .state(RETURNED)
            .build());

        verify(feeService, never()).getFeesDataForOrders(any());
        assertThat(response.getData()).containsEntry("amountToPay", null);
        assertThat(response.getData()).doesNotContainKeys("displayAmountToPay");
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

    @Test
    void shouldHaveSupplementAndDocumentIfC1OrderChosen() {
        given(feeService.getFeesDataForOrders(any())).willThrow(new FeeRegisterException(300, "duplicate", null));

        CaseDetails details = populatedCaseDetails();
        // Use a C1 supplement document
        details.getData().put("orders", Orders.builder().orderType(List.of(OrderType.CHILD_ASSESSMENT_ORDER)).build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(details);

        assertThat(callbackResponse.getData())
                .containsEntry("draftSupplement",
                        of("document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                                "document_filename", "file.pdf",
                                "document_binary_url",
                                "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary"));
        assertThat(callbackResponse.getData())
                .containsEntry("draftApplicationDocument",
                        of("document_url", "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4",
                                "document_filename", "file.pdf",
                                "document_binary_url",
                                "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary"));
    }


    @Test
    void shouldAddGeneratedCaseNameToCaseData() {
        given(feeService.getFeesDataForOrders(any())).willReturn(feesData(10));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData().toBuilder()
            .caseName("Draft title")
            .build());

        assertThat(callbackResponse.getData()).containsEntry("caseName", "LA & Respondent 1, Etc, Etc");
    }

    private static FeesData feesData(long amount) {
        return FeesData.builder()
            .totalAmount(BigDecimal.valueOf(amount))
            .build();
    }
}
