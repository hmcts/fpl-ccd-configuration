package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlagsType;
import uk.gov.hmcts.reform.fpl.model.caseflag.FlagDetailType;
import uk.gov.hmcts.reform.fpl.model.caseflag.ListTypeItem;
import uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.YES;

@WebMvcTest(CaseFlagsController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseFlagsControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final long CASE_REFERENCE = 12345L;
    private static final String CASE_NAME = "Case Name";
    private static final String INACTIVE = "Inactive";
    private static final String NOT_RELEVANT = "Not relevant";

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private CaseFlagsService caseFlagsService;

    CaseFlagsControllerAboutToSubmitTest() {
        super("caseFlags");
    }

    @Test
    void shouldProcessCaseFlagsAndReturnUpdatedDependentFlags() throws Exception {
        AboutToStartOrSubmitCallbackResponse response = postCaseFlagsAboutToSubmit(CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(caseDetailsData(
                "applicantFlags", caseFlags(SIGN_LANGUAGE_INTERPRETER, ACTIVE),
                "respondent1ExternalFlags", caseFlags(DISRUPTIVE_CUSTOMER, ACTIVE)
            ))
            .build());

        CaseData responseCaseData = extractCaseData(response);
        assertThat(response.getData()).containsEntry("caseName", CASE_NAME);
        assertThat(responseCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(responseCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(YES);

        ArgumentCaptor<CaseData> captor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseFlagsService).processNewlySetCaseFlags(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(CASE_REFERENCE);
        assertThat(captor.getValue().getApplicantFlags().getDetails()).hasSize(1);
        assertThat(captor.getValue().getRespondent1ExternalFlags().getDetails()).hasSize(1);
    }

    @Test
    void shouldReturnNoDependentFlagsWhenThereAreNoPartyFlags() throws Exception {
        AboutToStartOrSubmitCallbackResponse response = postCaseFlagsAboutToSubmit(CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(caseDetailsData())
            .build());

        CaseData responseCaseData = extractCaseData(response);
        assertThat(response.getData()).containsEntry("caseName", CASE_NAME);
        assertThat(responseCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(responseCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
        verify(caseFlagsService).processNewlySetCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldReturnNoDependentFlagsWhenRelevantFlagsAreInactiveOrAbsent() throws Exception {
        AboutToStartOrSubmitCallbackResponse response = postCaseFlagsAboutToSubmit(CaseDetails.builder()
            .id(CASE_REFERENCE)
            .data(caseDetailsData(
                "applicantFlags", caseFlags(LANGUAGE_INTERPRETER, INACTIVE),
                "applicantExternalFlags", caseFlags(VEXATIOUS_LITIGANT, INACTIVE),
                "respondent1Flags", caseFlags(NOT_RELEVANT, ACTIVE)
            ))
            .build());

        CaseData responseCaseData = extractCaseData(response);
        assertThat(responseCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(responseCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
        verify(caseFlagsService).processNewlySetCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/caseFlags/aboutToSubmit")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(caseFlagsService, never()).processNewlySetCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/caseFlags/aboutToSubmit")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("malformed json"))
            .andExpect(status().isBadRequest());

        verify(caseFlagsService, never()).processNewlySetCaseFlags(any(CaseData.class));
    }

    private AboutToStartOrSubmitCallbackResponse postCaseFlagsAboutToSubmit(CaseDetails caseDetails)
        throws Exception {
        byte[] response = mockMvc.perform(post("/caseFlags/aboutToSubmit")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(CallbackRequest.builder()
                    .caseDetails(caseDetails)
                    .build())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        return mapper.readValue(response, AboutToStartOrSubmitCallbackResponse.class);
    }

    private Map<String, Object> caseDetailsData(Object... entries) {
        Map<String, Object> data = new HashMap<>();
        data.put("caseName", CASE_NAME);

        for (int i = 0; i < entries.length; i += 2) {
            data.put((String) entries[i], entries[i + 1]);
        }

        return data;
    }

    private CaseFlagsType caseFlags(String flagName, String status) {
        return CaseFlagsType.builder()
            .details(ListTypeItem.from(FlagDetailType.builder()
                .name(flagName)
                .status(status)
                .build()))
            .build();
    }
}
