package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerGetDocMidEventTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    ManageDocumentsControllerGetDocMidEventTest() {
        super("manage-docs");
    }
}
