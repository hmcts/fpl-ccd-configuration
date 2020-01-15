package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ValidateEmergencyProtectionOrderControllerTest extends AbstractControllerTest {

    @Autowired
    private Time time;

    ValidateEmergencyProtectionOrderControllerTest() {
        super("validate-emergency-protection-order");
    }

    @Test
    void shouldReturnErrorsWhenEPOTypeIsPreventRemovalButAddressIsIncomplete() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now());
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "address");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce(
            "Enter a valid address for the contact",
            "Enter a postcode for the contact");
    }

    @Test
    void shouldNotReturnErrorsWhenEPOTypeIsRemoveToAccommodation() {
        CaseDetails caseDetails = createCaseDetails(REMOVE_TO_ACCOMMODATION, time.now());
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "address");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenEndDateIsNotWithinTheNextEightDays() {
        LocalDateTime nowPlusNineDays = time.now().plusDays(9);
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, nowPlusNineDays);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Date must be within the next 8 days");
    }

    @Test
    void shouldNotReturnErrorsWhenEndDateIsWithinTheNextEightDays() {
        LocalDateTime nowPlusSevenDays = time.now().plusDays(7);
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, nowPlusSevenDays);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private CaseDetails createCaseDetails(EPOType epoType, LocalDateTime localDateTime) {
        return CaseDetails.builder()
            .data(Map.of(
                "epoType", epoType,
                "epoRemovalAddress", Address.builder().build(),
                "epoEndDate", localDateTime
            )).build();
    }
}
