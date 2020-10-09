package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@ActiveProfiles("integration-test")
@WebMvcTest(ValidateOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ValidateOrderControllerTest extends AbstractControllerTest {

    private static final OrderTypeAndDocument ORDER_TYPE_AND_DOCUMENT = OrderTypeAndDocument.builder()
        .type(GeneratedOrderType.BLANK_ORDER)
        .build();
    private static final OrderTypeAndDocument INTERIM_ORDER_TYPE_AND_DOCUMENT = OrderTypeAndDocument.builder()
        .subtype(GeneratedOrderSubtype.INTERIM)
        .build();

    @Autowired
    private Time time;

    ValidateOrderControllerTest() {
        super("validate-order");
    }

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().plusDays(1),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Date of issue cannot be in the future");
    }

    @Test
    void shouldNotReturnErrorsWhenDateOfIssueIsToday() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenDateOfIssueIsInPast() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().minusDays(1),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenInterimOrderSubtypeAndInterimEndDateInTheFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(),
            NAMED_DATE, INTERIM_ORDER_TYPE_AND_DOCUMENT, time.now().plusDays(1));
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenInterimOrderSubtypeAndInterimEndDateNotSet() {
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(CaseDetails.builder()
            .data(Map.of(
                "dateOfIssue", time.now().toLocalDate(),
                "orderTypeAndDocument", INTERIM_ORDER_TYPE_AND_DOCUMENT
            )).build(), "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenInterimOrderSubtypeAndInterimEndDateToday() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(),
            NAMED_DATE, INTERIM_ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWhenInterimOrderSubtypeAndInterimEndDateInThePast() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(),
            NAMED_DATE, INTERIM_ORDER_TYPE_AND_DOCUMENT, time.now().minusDays(1));
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
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
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().minusDays(1),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWhenTheInterimEndDateIsToday() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now(),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldNotReturnErrorsWhenTheInterimEndDateIsInTheFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().plusDays(1),
            NAMED_DATE, ORDER_TYPE_AND_DOCUMENT, time.now().plusDays(1));
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenTheInterimEndDateTypeIsEndOfProceedings() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, time.now().minusDays(1),
            END_OF_PROCEEDINGS, ORDER_TYPE_AND_DOCUMENT, time.now().minusDays(1));
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "interim-end-date");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenNoChildIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("childSelector", Selector.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "child-selector");

        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Select the children included in the order.");
    }

    @Test
    void shouldNotReturnErrorsWhenAChildIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("childSelector", Selector.builder().selected(List.of(0)).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "child-selector");

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoCareOrderSelection() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(emptyMap())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "care-orders-selection");

        assertThat(response.getErrors()).containsExactly("Select care orders to be discharged.");
    }

    @Test
    void shouldReturnErrorWhenNoCareOrderIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("careOrderSelector", Selector.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "care-orders-selection");

        assertThat(response.getErrors()).containsExactly("Select care orders to be discharged.");
    }

    @Test
    void shouldNotReturnErrorsWhenCareOrderIsSelected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("careOrderSelector", Selector.builder().selected(List.of(0)).build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, "care-orders-selection");

        assertThat(response.getErrors()).isEmpty();
    }

    private CaseDetails createCaseDetails(EPOType preventRemoval, LocalDateTime now) {
        return createCaseDetails(preventRemoval, now, END_OF_PROCEEDINGS, ORDER_TYPE_AND_DOCUMENT, time.now());
    }

    private CaseDetails createCaseDetails(EPOType epoType, LocalDateTime localDateTime,
                                          InterimEndDateType interimEndDateType,
                                          OrderTypeAndDocument orderTypeAndDocument,
                                          LocalDateTime interimEndDateTime) {

        return CaseDetails.builder()
            .data(Map.of(
                "dateOfIssue", localDateTime.toLocalDate(),
                "epoType", epoType,
                "epoRemovalAddress", Address.builder().build(),
                "epoEndDate", localDateTime,
                "interimEndDate", InterimEndDate.builder()
                    .type(interimEndDateType)
                    .endDate(interimEndDateTime.toLocalDate())
                    .build(),
                "orderTypeAndDocument", orderTypeAndDocument
            )).build();
    }
}
