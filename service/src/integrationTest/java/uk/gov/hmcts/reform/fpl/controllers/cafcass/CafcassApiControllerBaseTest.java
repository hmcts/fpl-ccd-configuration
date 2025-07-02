package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;
import uk.gov.hmcts.reform.fpl.interceptors.CafcassApiInterceptor;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public abstract class CafcassApiControllerBaseTest extends AbstractTest {
    private static final UserInfo CAFCASS_SYSTEM_UPDATE_USER_INFO = UserInfo.builder()
        .roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName()))
        .build();

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private CafcassApiInterceptor cafcassApiInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        givenCurrentUser(CAFCASS_SYSTEM_UPDATE_USER_INFO);
        when(cafcassApiInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    protected MvcResult sendRequest(MockHttpServletRequestBuilder request, int expectedStatus) throws Exception {
        MvcResult response = mockMvc.perform(request).andExpect(status().is(expectedStatus)).andReturn();
        assertEquals(response.getResponse().getStatus(), expectedStatus);
        return response;
    }

    protected <T> T readResponseContent(MvcResult response, Class<T> responseType) throws IOException {
        return mapper.readValue(response.getResponse().getContentAsByteArray(), responseType);
    }

    protected MockHttpServletRequestBuilder buildGetRequest(String url) {
        return get(url).header("authorization", USER_AUTH_TOKEN);
    }

    protected MockHttpServletRequestBuilder buildPostRequest(String url) {
        return post(url).header("authorization", USER_AUTH_TOKEN);
    }
}
