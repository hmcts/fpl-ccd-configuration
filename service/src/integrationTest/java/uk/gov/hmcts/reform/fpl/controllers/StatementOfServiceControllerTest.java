package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(StatementOfServiceController.class)
@OverrideAutoConfiguration(enabled = true)
class StatementOfServiceControllerTest extends AbstractCallbackTest {

    StatementOfServiceControllerTest() {
        super("statement-of-service");
    }

    @BeforeEach
    void mockUserNameRetrieval() {
        givenCurrentUserWithName("Emma Taylor");
    }

    @Test
    void shouldPrepopulateRecipient() {
        CaseData caseData = CaseData.builder().build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getData())
            .containsKey("statementOfService")
            .containsEntry("serviceDeclarationLabel", "I, Emma Taylor, have served the documents as stated.");
    }
}

