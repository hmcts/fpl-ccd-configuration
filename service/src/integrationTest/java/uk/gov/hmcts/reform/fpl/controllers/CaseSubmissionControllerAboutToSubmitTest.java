package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    private Document document = document();

    CaseSubmissionControllerAboutToSubmitTest() {
        super("case-submission");
    }

    @BeforeEach
    void mocking() {
        byte[] pdf = {1, 2, 3, 4, 5};

        given(userDetailsService.getUserName(userAuthToken))
            .willReturn("Emma Taylor");
        given(documentGeneratorService.generateSubmittedFormPDF(any(), any()))
            .willReturn(pdf);
        given(uploadDocumentService.uploadPDF(pdf, "2313.pdf"))
            .willReturn(document);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithNoData() {
        postAboutToSubmitEvent(new byte[]{}, SC_BAD_REQUEST);
    }

    @Test
    void shouldReturnUnsuccessfulResponseWithMalformedData() {
        postAboutToSubmitEvent("malformed json".getBytes(), SC_BAD_REQUEST);
    }

    @Test
    void shouldSetCtscPropertyToYesWhenCtscLaunchDarklyVariableIsEnabled() {
        given(featureToggleService.isCtscEnabled()).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData())
            .containsEntry("caseLocalAuthority", "example")
            .containsEntry("sendToCtsc", "No")
            .containsEntry("submittedForm", ImmutableMap.<String, String>builder()
                .put("document_url", document.links.self.href)
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .build());
    }

    @Test
    void shouldSetCtscPropertyToNoWhenCtscLaunchDarklyVariableIsDisabled() {
        given(featureToggleService.isCtscEnabled()).willReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData()).containsEntry("sendToCtsc", "Yes");
    }

    @Test
    void shouldRemoveTemporaryFieldsWhenPresent() {
        given(featureToggleService.isCtscEnabled()).willReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(CaseDetails.builder()
            .id(2313L)
            .data(Map.of(
                "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                "caseLocalAuthority", "example",
                "amountToPay", "233300",
                "displayAmountToPay", "Yes"
            ))
            .build());

        assertThat(callbackResponse.getData()).doesNotContainKeys("displayAmountToPay", "amountToPay");
    }
}
