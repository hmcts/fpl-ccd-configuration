package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static java.util.Map.of;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CaseSubmissionService caseSubmissionService;

    private final Document document = document();

    CaseSubmissionControllerAboutToSubmitTest() {
        super("case-submission");
    }

    @BeforeEach
    void mocking() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name("Emma Taylor").build());
        given(caseSubmissionService.generateSubmittedFormPDF(any(), eq(false)))
            .willReturn(document);
        given(uploadDocumentService.uploadPDF(DOCUMENT_CONTENT, "2313.pdf"))
            .willReturn(document);
        given(featureToggleService.isRestrictedFromCaseSubmission("FPLA"))
            .willReturn(true);
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
        given(featureToggleService.isCtscEnabled(anyString())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData())
            .containsEntry("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE)
            .containsEntry("sendToCtsc", "No")
            .containsEntry("submittedForm", ImmutableMap.<String, String>builder()
                .put("document_url", document.links.self.href)
                .put("document_binary_url", document.links.binary.href)
                .put("document_filename", document.originalDocumentName)
                .build());
    }

    @Test
    void shouldSetCtscPropertyToNoWhenCtscLaunchDarklyVariableIsDisabled() {
        given(featureToggleService.isCtscEnabled(anyString())).willReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent("fixtures/case.json");

        assertThat(callbackResponse.getData()).containsEntry("sendToCtsc", "Yes");
        verify(featureToggleService).isCtscEnabled(LOCAL_AUTHORITY_1_NAME);
    }

    @Test
    void shouldRetainPaymentInformationInCase() {
        given(featureToggleService.isCtscEnabled(anyString())).willReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(CaseDetails.builder()
            .id(2313L)
            .data(Map.of(
                "dateSubmitted", dateNow(),
                "orders", Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "amountToPay", "233300",
                "displayAmountToPay", "Yes"
            ))
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("amountToPay", "233300")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Nested
    class LocalAuthorityValidation {

        final String localAuthority = LOCAL_AUTHORITY_1_CODE;
        final CaseDetails caseDetails = CaseDetails.builder()
            .data(of("caseLocalAuthority", localAuthority))
            .build();

        @Test
        void shouldReturnErrorWhenCaseSubmissionIsBlockedForLocalAuthority() {
            given(featureToggleService.isRestrictedFromCaseSubmission(localAuthority)).willReturn(true);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", localAuthority);
            assertThat(callbackResponse.getErrors()).contains("You cannot submit this application online yet."
                + " Ask your FPL administrator for your local authority’s enrolment date");
        }

        @Test
        void shouldReturnNoErrorsWhenCaseSubmissionIsAllowedForLocalAuthority() {
            given(featureToggleService.isRestrictedFromCaseSubmission(localAuthority)).willReturn(false);

            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", localAuthority);
            assertThat(callbackResponse.getErrors()).isEmpty();
        }
    }
}
