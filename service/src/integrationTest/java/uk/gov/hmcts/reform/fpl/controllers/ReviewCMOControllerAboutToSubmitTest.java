package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.controllers.cmo.ReviewCMOController;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewCMOControllerAboutToSubmitTest extends AbstractControllerTest {

    protected ReviewCMOControllerAboutToSubmitTest() {
        super("review-cmo");
    }

    @Test
    void shouldSetReturnStatusAndRequestedChangesWhenJudgeRejectsOrder() {

    }

    @Test
    void shouldSealPDFAndAddCMOToListWhenJudgeApprovesOrder() {

    }

    @Test
    void shouldUpdateDraftCMOListToReflectJudgeDecision() {

    }
}
