package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ActiveProfiles("integration-test")
@WebMvcTest(ReturnApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationAboutToSubmitTest extends AbstractControllerTest {
    ReturnApplicationAboutToSubmitTest() {
        super("return-application");
    }

    @Autowired
    private Time time;

    @Test
    void shouldMigrateSubmittedDocumentToReturnedDocumentBundle() {
        DocumentReference documentReference = buildDocumentReference();

        Map<String, Object> data = ImmutableMap.of(
            RETURN_APPLICATION, ReturnApplication.builder()
                .note("Some note")
                .reason(List.of(INCOMPLETE))
                .build(),
            "dateSubmitted", "2050-05-19",
            "submittedForm", documentReference);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(buildCaseDetails(data));
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        ReturnApplication returnApplication = caseData.getReturnApplication();
        DocumentReference returnedDocumentReference = returnApplication.getDocument();

        assertThat(callbackResponse.getData().get("submittedForm")).isNull();
        assertThat(returnedDocumentReference.getFilename()).isEqualTo("mockSubmittedForm_returned.pdf");
        assertThat(returnedDocumentReference.getUrl()).isEqualTo("http://mock-dm-store");
        assertThat(returnedDocumentReference.getBinaryUrl()).isEqualTo("http://mock-dm-store");
        assertThat(returnApplication.getSubmittedDate()).isEqualTo("19 May 2050");
        assertThat(returnApplication.getReturnedDate()).isEqualTo(getFormattedDate());
        assertThat(returnApplication.getNote()).isEqualTo("Some note");
        assertThat(returnApplication.getReason()).isEqualTo(List.of(INCOMPLETE));
    }

    private String getFormattedDate() {
        return formatLocalDateToString(time.now().toLocalDate(), "dd MMM YYYY");
    }

    private DocumentReference buildDocumentReference() {
        return DocumentReference.builder()
            .filename("mockSubmittedForm.pdf")
            .url("http://mock-dm-store")
            .binaryUrl("http://mock-dm-store")
            .build();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .data(data)
            .state(OPEN.getValue())
            .build();
    }
}
