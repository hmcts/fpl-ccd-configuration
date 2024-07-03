package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public class CafcassCasesControllerTest extends AbstractTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchCases() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/cases")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(","))
                .queryParam("startDate", "2023-03-28T12:32:54.541")
                .queryParam("endDate", "2024-03-27T12:32:54.542"))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("searchCases - Start date: [2023-03-28], End date: [2024-03-27]",
            response.getResponse().getContentAsString());
    }

    @Test
    void searchCasesInvalidFormat400() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/cases")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(","))
                .queryParam("startDate", "123")
                .queryParam("endDate", "321"))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }

    @Test
    void searchCasesEmptyParam400() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/cases"))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);

        response = mockMvc
            .perform(get("/cases")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(","))
                .queryParam("startDate", "2023-03-28T12:32:54.541"))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);

        response = mockMvc
            .perform(get("/cases")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(","))
                .queryParam("endDate", "2024-03-27T12:32:54.542"))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }

    @Test
    void searchCases500() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/cases")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(","))
                .queryParam("startDate", "2024-03-28T12:32:54.541")
                .queryParam("endDate", "2023-03-27T12:32:54.542"))
            .andExpect(status().is(500))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 500);
    }

    @Test
    void getDocumentBinary() throws Exception {
        UUID docId = UUID.randomUUID();
        MvcResult response = mockMvc
            .perform(get("/cases/documents/%s/binary".formatted(docId))
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("getDocumentBinary - document id: [%s]".formatted(docId),
            response.getResponse().getContentAsString());
    }

    @Test
    void getDocumentBinary400() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/cases/documents/123/binary")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);

        response = mockMvc
            .perform(get("/cases/documents/ /binary")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }

    @Test
    void uploadDocument() throws Exception {
        UUID caseId = UUID.randomUUID();
        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .file(file)
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("uploadDocument - caseId: [%s], file length: [%s], typeOfDocument: [%s]"
                .formatted(caseId, fileBytes.length, "type Of Document"),
            response.getResponse().getContentAsString());
    }

    @Test
    void uploadDocument400() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);


        byte[] fileBytes = "This is a file. Trust me!".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "MOCK_FILE.pdf", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        response = mockMvc
            .perform(MockMvcRequestBuilders.multipart("/cases/%s/document".formatted(caseId))
                .file(file)
                .param("typeOfDocument", "type Of Document")
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }

    @Test
    void uploadGuardians() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc.perform(post("/cases/%s/guardians".formatted(caseId))
                .content("[\n"
                         + "  {\n"
                         + "    \"guardianName\": \"John Smith\",\n"
                         + "    \"telephoneNumber\": \"01234567890\",\n"
                         + "    \"email\": \"john.smith@example.com\",\n"
                         + "    \"children\": [\n"
                         + "      \"Joe Bloggs\"\n"
                         + "    ]\n"
                         + "  }\n"
                         + "]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(200))
            .andReturn();

        assertEquals("uploadGuardians - caseId: [%s], no of guardians: [%s]\nguardianName: [%s], children: [%s]\n"
                .formatted(caseId, 1, "John Smith", "Joe Bloggs"),
            response.getResponse().getContentAsString());
    }

    @Test
    void uploadGuardians400() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc.perform(post("/cases/%s/guardians".formatted(caseId))
                .content("[]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);

        response = mockMvc.perform(post("/cases/%s/guardians".formatted(" "))
                .content("[\n"
                         + "  {\n"
                         + "    \"guardianName\": \"John Smith\",\n"
                         + "    \"telephoneNumber\": \"01234567890\",\n"
                         + "    \"email\": \"john.smith@example.com\",\n"
                         + "    \"children\": [\n"
                         + "      \"Joe Bloggs\"\n"
                         + "    ]\n"
                         + "  }\n"
                         + "]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(400))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 400);
    }

    @Test
    void uploadGuardians500() throws Exception {
        UUID caseId = UUID.randomUUID();

        MvcResult response = mockMvc.perform(post("/cases/%s/guardians".formatted(caseId))
                .content("[\n"
                         + "  {\n"
                         + "    \"guardianName\": \"John Smith\",\n"
                         + "    \"telephoneNumber\": \"01234567890\",\n"
                         + "    \"email\": \"john.smith@example.com\",\n"
                         + "    \"children\": \"12313\""
                         + "  }\n"
                         + "]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", USER_AUTH_TOKEN)
                .header("user-id", USER_ID)
                .header("user-roles", String.join(",")))
            .andExpect(status().is(500))
            .andReturn();

        assertEquals(response.getResponse().getStatus(), 500);
    }
}
