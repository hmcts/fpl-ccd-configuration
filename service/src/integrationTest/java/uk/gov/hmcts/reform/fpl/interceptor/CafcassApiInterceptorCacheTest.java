package uk.gov.hmcts.reform.fpl.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;
import uk.gov.hmcts.reform.fpl.controllers.cafcass.CafcassCasesController;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFeatureFlag;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiDocumentService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public class CafcassApiInterceptorCacheTest extends AbstractTest {
    private static final String AUTH_TOKEN_TEST = "bearerToken cafcass";
    private static final UserInfo CAFCASS_SYSTEM_UPDATE_USER =
        UserInfo.builder().roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName())).build();

    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CafcassApiDocumentService cafcassApiDocumentService;

    @Test
    public void shouldCacheIdamUserResult() throws Exception {
        when(featureToggleService.getCafcassAPIFlag())
            .thenReturn(CafcassApiFeatureFlag.builder().enableApi(true).build());
        when(idamClient.getUserInfo(AUTH_TOKEN_TEST)).thenReturn(CAFCASS_SYSTEM_UPDATE_USER);

        UUID docId = UUID.randomUUID();
        byte[] docBinary = "This is a document".getBytes();

        when(cafcassApiDocumentService.downloadDocumentByDocumentId(docId.toString())).thenReturn(docBinary);

        MvcResult rsp = sendRequest(buildGetRequest(String.format("/cases/documents/%s/binary", docId)), 200);
        assertArrayEquals(rsp.getResponse().getContentAsByteArray(), docBinary);

        when(idamClient.getUserInfo(AUTH_TOKEN_TEST)).thenReturn(null);

        // verify idam result is updated
        assertNull(idamClient.getUserInfo(AUTH_TOKEN_TEST));
        // should use the cache
        rsp = sendRequest(buildGetRequest(String.format("/cases/documents/%s/binary", docId)), 200);
        assertArrayEquals(rsp.getResponse().getContentAsByteArray(), docBinary);
    }

    private MvcResult sendRequest(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        MvcResult response = mockMvc.perform(request).andExpect(status().is(expectedStatus)).andReturn();
        assertEquals(response.getResponse().getStatus(), expectedStatus);
        return response;
    }

    private MockHttpServletRequestBuilder buildGetRequest(String url) {
        return get(url).header("authorization", AUTH_TOKEN_TEST);
    }
}
