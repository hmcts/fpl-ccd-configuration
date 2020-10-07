package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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

        CallbackRequest request = toCallBackRequest(asCaseDetails(caseData), asCaseDetails(CaseData.builder().build()));
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = extractCaseData(response).getStandardDirectionOrder();

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }

    @Test
    void shouldAddAlreadyUploadedDocumentToConstructedStandardDirectionOrder() {
        CaseData caseDataBefore = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().orderDoc(DOCUMENT).build())
            .build();

        CallbackRequest request = toCallBackRequest(
            asCaseDetails(CaseData.builder().build()),
            asCaseDetails(caseDataBefore)
        );
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(request, "upload-route");

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(DOCUMENT).build();
        StandardDirectionOrder builtOrder = extractCaseData(response).getStandardDirectionOrder();

        assertThat(builtOrder).isEqualTo(expectedOrder);
    }
}
