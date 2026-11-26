package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(CaseFlagsMigrationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseFlagsMigrationControllerTest extends AbstractCallbackTest {

    private static final long CASE_ID = 12345L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureToggleService featureToggleService;

    @SpyBean
    private CaseFlagsService caseFlagsService;

    CaseFlagsMigrationControllerTest() {
        super("case-flags");
    }

    @Test
    void shouldMigrateCaseFlagsWhenV2IsEnabled() throws Exception {
        given(featureToggleService.isCaseFlagsV2Enabled()).willReturn(true);

        AboutToStartOrSubmitCallbackResponse response = postCallback(
            "/callback/case-flags/migration/about-to-submit", caseDetails());

        assertThat(response.getData()).containsKeys(
            "caseFlags", "applicantFlags", "applicantExternalFlags",
            "respondent1Flags", "respondent1ExternalFlags"
        );
        verify(caseFlagsService).migrateCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldSkipMigrationWhenV2IsDisabled() throws Exception {
        given(featureToggleService.isCaseFlagsV2Enabled()).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postCallback(
            "/callback/case-flags/migration/about-to-submit", caseDetails());

        assertThat(response.getData())
            .containsEntry("caseFlags", null)
            .containsEntry("applicantFlags", null)
            .containsEntry("respondent1Flags", null);
        verify(caseFlagsService, never()).migrateCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldRollbackCaseFlagsWhenV2IsEnabled() throws Exception {
        given(featureToggleService.isCaseFlagsV2Enabled()).willReturn(true);

        CaseDetails caseDetails = caseDetails();
        caseDetails.getData().put("caseFlags", new HashMap<>());
        caseDetails.getData().put("applicantFlags", new HashMap<>());

        AboutToStartOrSubmitCallbackResponse response = postCallback(
            "/callback/case-flags/rollback/about-to-submit", caseDetails);

        assertThat(response.getData())
            .containsEntry("caseFlags", null)
            .containsEntry("applicantFlags", null)
            .containsEntry("representative10ExternalFlags", null);
        verify(caseFlagsService).rollbackCaseFlags(any(CaseData.class));
    }

    @Test
    void shouldSkipRollbackWhenV2IsDisabled() throws Exception {
        given(featureToggleService.isCaseFlagsV2Enabled()).willReturn(false);

        CaseDetails caseDetails = caseDetails();
        Map<String, Object> caseFlags = new HashMap<>();
        Map<String, Object> applicantFlags = new HashMap<>();
        caseDetails.getData().put("caseFlags", caseFlags);
        caseDetails.getData().put("applicantFlags", applicantFlags);

        AboutToStartOrSubmitCallbackResponse response = postCallback(
            "/callback/case-flags/rollback/about-to-submit", caseDetails);

        assertThat(response.getData())
            .containsEntry("caseFlags", caseFlags)
            .containsEntry("applicantFlags", applicantFlags);
        verify(caseFlagsService, never()).rollbackCaseFlags(any(CaseData.class));
    }

    private AboutToStartOrSubmitCallbackResponse postCallback(String url, CaseDetails caseDetails) throws Exception {
        byte[] response = mockMvc.perform(post(url)
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(CallbackRequest.builder().caseDetails(caseDetails).build())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        return mapper.readValue(response, AboutToStartOrSubmitCallbackResponse.class);
    }

    private CaseDetails caseDetails() {
        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(LocalAuthority.builder().name("Test local authority").build())))
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder().firstName("Joe").lastName("Bloggs").build())
                .build())))
            .build();

        return CaseDetails.builder()
            .id(CASE_ID)
            .data(new HashMap<>(mapper.convertValue(caseData, new TypeReference<Map<String, Object>>() { })))
            .build();
    }
}
