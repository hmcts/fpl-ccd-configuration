package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.fpl.controllers.AbstractTest;

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

    private <T> T getEvent(String path, Class<T> responseType, String... userRoles) {
        try {
            MvcResult response = mockMvc
                .perform(get(path)
                    .header("user-roles", String.join(",", userRoles)))
                .andReturn();

            byte[] responseBody = response.getResponse().getContentAsByteArray();

            if (responseBody.length > 0) {
                return mapper.readValue(responseBody, responseType);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T postEvent(String path, byte[] data, Class<T> responseType, String... userRoles) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
                    .header("user-roles", String.join(",", userRoles))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(data))
                .andReturn();

            byte[] responseBody = response.getResponse().getContentAsByteArray();

            if (responseBody.length > 0) {
                return mapper.readValue(responseBody, responseType);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toBytes(Object o) {
        try {
            return mapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
