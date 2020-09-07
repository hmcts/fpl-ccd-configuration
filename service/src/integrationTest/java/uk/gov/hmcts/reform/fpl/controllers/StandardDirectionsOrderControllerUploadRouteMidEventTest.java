package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class StandardDirectionsOrderControllerUploadRouteMidEventTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().filename("prepared.pdf").build();

    StandardDirectionsOrderControllerUploadRouteMidEventTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldAddPreparedSDOToConstructedStandardDirectionOrder() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(DOCUMENT)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = mapper.convertValue(
            response.getData().get("standardDirectionOrder"),
            StandardDirectionOrder.class
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }

    @Test
    void shouldAddAlreadyUploadedDocumentToConstructedStandardDirectionOrder() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderDoc(DOCUMENT).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = mapper.convertValue(
            response.getData().get("standardDirectionOrder"),
            StandardDirectionOrder.class
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }
}
