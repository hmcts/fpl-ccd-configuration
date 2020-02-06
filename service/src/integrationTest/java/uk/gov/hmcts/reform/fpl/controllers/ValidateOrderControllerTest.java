package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@ActiveProfiles("integration-test")
@WebMvcTest(ValidateOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ValidateOrderControllerTest extends AbstractControllerTest {

    @Autowired
    private Time time;

    ValidateOrderControllerTest() {
        super("validate-order");
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
    void shouldReturnErrorsWhenTheEPOEndDateIsNotWithinTheNextEightDays() {
        LocalDateTime nowPlusNineDays = time.now().plusDays(9);
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, nowPlusNineDays);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "epo-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Date must be within the next 8 days");
    }

    @Test
    void shouldNotReturnErrorsWhenTheEPOEndDateIsWithinTheNextEightDays() {
        LocalDateTime nowPlusSevenDays = time.now().plusDays(7);
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, nowPlusSevenDays);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "epo-end-date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenTheInterimEndDateIsNotInTheFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().minusDays(1), NAMED_DATE);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWhenTheInterimEndDateIsToday() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(), NAMED_DATE);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldNotReturnErrorsWhenTheInterimEndDateIsInTheFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().plusDays(1), NAMED_DATE);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenTheInterimEndDateTypeIsEndOfProceedings() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().minusDays(1), END_OF_PROCEEDINGS);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenAChildIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("childSelector", ChildSelector.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "child-selector");

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Select the children included in the order.");
    }

    @Test
    void shouldNotReturnErrorsWhenAChildIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("childSelector", ChildSelector.builder().selected(List.of(0)).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "child-selector");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private CaseDetails createCaseDetails(EPOType preventRemoval, LocalDateTime now) {
        return createCaseDetails(preventRemoval, now, END_OF_PROCEEDINGS);
    }

    private CaseDetails createCaseDetails(EPOType epoType, LocalDateTime localDateTime,
                                          InterimEndDateType interimEndDateType) {
        return CaseDetails.builder()
            .data(Map.of(
                "epoType", epoType,
                "epoRemovalAddress", Address.builder().build(),
                "epoEndDate", localDateTime,
                "interimEndDate", InterimEndDate.builder()
                    .type(interimEndDateType)
                    .endDate(localDateTime.toLocalDate())
                    .build()
            )).build();
    }
}
