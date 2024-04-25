package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsMidEventControllerTest extends AbstractCallbackTest {
    private static final String ERROR_MESSAGE = "Date received cannot be in the future";

    @MockBean
    private FeeService feeService;

    UploadC2DocumentsMidEventControllerTest() {
        super("upload-c2");
    }

    @Test
    void shouldAddAmountToPayField() {
        given(feeService.getFeesDataForC2(WITH_NOTICE)).willReturn(FeesData.builder()
            .totalAmount(BigDecimal.TEN)
            .build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        verify(feeService).getFeesDataForC2(WITH_NOTICE);
        assertThat(response.getData())
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldRemoveTemporaryC2DocumentForEmptyUrl() {
        given(feeService.getFeesDataForC2(WITH_NOTICE)).willReturn(FeesData.builder()
            .totalAmount(BigDecimal.TEN)
            .build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document",
                Map.of("document", Map.of()), "c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        CaseData updatedCaseData = extractCaseData(CaseDetails.builder().data(response.getData()).build());
        assertThat(updatedCaseData).extracting("temporaryC2Document").extracting("document")
            .isNull();
    }

    @Test
    void shouldKeepTemporaryC2DocumentForNonEmptyUrl() {
        given(feeService.getFeesDataForC2(WITH_NOTICE)).willReturn(FeesData.builder()
            .totalAmount(BigDecimal.TEN)
            .build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document",
                Map.of("document", Map.of("url", "example_url")),
                "c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        assertThat(response.getData()).extracting("temporaryC2Document")
            .extracting("document")
            .extracting("url")
            .isEqualTo("example_url");
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(feeService.getFeesDataForC2(any())).willThrow((new FeeRegisterException(1, "", new Throwable())));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        assertThat(response.getData())
            .doesNotContainKey("amountToPay")
            .containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldDisplayErrorForInvalidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("pbaNumber", "12345")))
            .build(), "validate");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getData()).extracting("temporaryC2Document").extracting("pbaNumber").isEqualTo("PBA12345");
    }

    @Test
    void shouldNotDisplayErrorForValidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("pbaNumber", "1234567")))
            .build(), "validate");

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).extracting("temporaryC2Document")
            .extracting("pbaNumber")
            .isEqualTo("PBA1234567");
    }

    @Test
    void shouldDisplayErrorForInvalidPBANumberAndInvalidDateReceivedInSupportingDocs() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document",
                Map.of(
                    "supportingEvidenceBundle", wrapElements(createSupportingEvidenceBundle()),
                    "pbaNumber", "12345")))
            .build(), "validate");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getErrors()).contains(ERROR_MESSAGE);
    }

    private SupportingEvidenceBundle createSupportingEvidenceBundle() {
        return SupportingEvidenceBundle.builder()
            .name("Supporting document")
            .notes("Document notes")
            .dateTimeReceived(LocalDateTime.now().plusDays(1))
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference())
            .build();
    }
}
