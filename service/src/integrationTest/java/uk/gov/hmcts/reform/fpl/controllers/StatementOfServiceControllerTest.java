package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@WebMvcTest(StatementOfServiceController.class)
@OverrideAutoConfiguration(enabled = true)
class StatementOfServiceControllerTest extends AbstractControllerTest {

    @MockBean
    private UserDetailsService userDetailsService;

    StatementOfServiceControllerTest() {
        super("statement-of-service");
    }

    @BeforeEach
    void mockUserNameRetrieval() {
        given(userDetailsService.getUserName()).willReturn("Emma Taylor");
    }

    @Test
    void shouldPrepopulateRecipient() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData())
            .containsKey("statementOfService")
            .containsEntry("serviceDeclarationLabel", "I, Emma Taylor, have served the documents as stated.");
    }
}

