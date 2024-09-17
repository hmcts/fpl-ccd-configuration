package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;
import uk.gov.hmcts.reform.fpl.interceptors.CafcassApiInterceptor;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@Deprecated
@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public class CafcassCasesControllerTest extends AbstractTest {
    private static final UserInfo CAFCASS_SYSTEM_UPDATE_USER_INFO = UserInfo.builder()
        .roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName()))
        .build();
    private static final UUID CASE_ID = UUID.randomUUID();
    private static final  byte[] FILE_BYTES = "This is a file. Trust me!".getBytes();
    private static final MockMultipartFile FILE = new MockMultipartFile(
        "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, FILE_BYTES);

    @MockBean
    private CafcassApiInterceptor cafcassApiInterceptor;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        when(cafcassApiInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        givenCurrentUser(CAFCASS_SYSTEM_UPDATE_USER_INFO);
    }

    @Test
    void uploadDocument() throws Exception {

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(CASE_ID))
                .file(FILE)
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("uploadDocument - caseId: [%s], file length: [%s], typeOfDocument: [%s]"
                .formatted(CASE_ID, FILE_BYTES.length, "type Of Document"),
            response.getResponse().getContentAsString());
    }

    @Test
    void uploadDocument400() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);


        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .file(file)
                .header("authorization", USER_AUTH_TOKEN))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }
}
