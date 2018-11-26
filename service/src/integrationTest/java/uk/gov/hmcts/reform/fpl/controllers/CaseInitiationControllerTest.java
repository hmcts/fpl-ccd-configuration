package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
//    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
//    private static final String JURISDICTION = "PUBLICLAW";
//    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";
    private static final String USER_ID = "1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private CaseAccessApi caseAccessApi;

    @Test
    void shouldAddCaseLocalAuthorityToCaseData() throws Exception {
        JSONObject expectedData = new JSONObject();
        expectedData.put("caseName", "title");
        expectedData.put("caseLocalAuthority", "EX");

        given(idamApi.retrieveUserDetails(AUTH_TOKEN)).willReturn(
            new UserDetails(null, "user@example.gov.uk", null, null, null));

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("caseName", "title")
                .build()).build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains(expectedData.toString());
    }

    @Test
    void shouldPopulateErrorsInResponseWhenDomainNameIsNotFound() throws Exception {
        AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add("The email address was not linked to a known Local Authority")
                .build())
            .build();

        given(idamApi.retrieveUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails(null, "user@email.gov.uk", null, null, null));

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/empty-case-details.json")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).isEqualTo(MAPPER.writeValueAsString(expectedResponse));
    }

    @Test
    void shouldGrantUsersAccessToCase() throws Exception {
        //        JSONObject expectedData = new JSONObject();
        //        expectedData.put("caseName", "title");
        //        expectedData.put("caseLocalAuthority", "EX");

        //        given(caseAccessApi.grantAccessToCase(
        //            AUTH_TOKEN, SERVICE_AUTH_TOKEN, "4", JURISDICTION, CASE_TYPE, "1",
        //            new UserId("1")));

        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .id(1L)
            .data(ImmutableMap.<String, Object>builder()
                .put("caseLocalAuthority", "example")
                .build()).build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        System.out.println("response = " + response);

//        assertThat(caseAccessApi.grantAccessToCase())

    }
}
