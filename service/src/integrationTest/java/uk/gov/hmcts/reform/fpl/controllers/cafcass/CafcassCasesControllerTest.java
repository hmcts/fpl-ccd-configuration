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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(CafcassCasesController.class)
@OverrideAutoConfiguration(enabled = true)
public class CafcassCasesControllerTest extends AbstractTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchCases() {
        getEvent("/cases/start_date=2023-03-28T12:32:54.541&end_date=2024-03-27T12:32:54.54", Object.class);
        assertTrue(true);
    }


    private <T> T getEvent(String path, Class<T> responseType, String... userRoles) {
        try {
            MvcResult response = mockMvc
                .perform(post(path)
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
