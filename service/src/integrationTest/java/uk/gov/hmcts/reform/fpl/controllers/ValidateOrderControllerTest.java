package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.REMOVE_TO_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.SPECIFIC_TIME_NAMED_DATE;

@WebMvcTest(ValidateOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class ValidateOrderControllerTest extends AbstractCallbackTest {

    private static final OrderTypeAndDocument ORDER_TYPE_AND_DOCUMENT = OrderTypeAndDocument.builder()
        .type(GeneratedOrderType.BLANK_ORDER)
        .build();
    private static final OrderTypeAndDocument INTERIM_ORDER_TYPE_AND_DOCUMENT = OrderTypeAndDocument.builder()
        .subtype(GeneratedOrderSubtype.INTERIM)
        .build();

    ValidateOrderControllerTest() {
        super("validate-order");
    }

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, now().plusDays(1), ORDER_TYPE_AND_DOCUMENT);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Date of issue cannot be in the future");
    }

    @Test
    void shouldNotReturnErrorsWhenDateOfIssueIsToday() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, now(), ORDER_TYPE_AND_DOCUMENT);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenDateOfIssueIsInPast() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, now().minusDays(1), ORDER_TYPE_AND_DOCUMENT);
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWithInterimOrderWhenEndDateInTheFuture() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(NAMED_DATE)
            .endDate(dateNow().plusDays(1))
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWithInterimOrderWhenEndDateToday() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(NAMED_DATE)
            .endDate(dateNow())
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWithInterimOrderWhenEndDateInThePast() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(NAMED_DATE)
            .endDate(dateNow().minusDays(1))
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldNotReturnErrorsWithInterimOrderWhenEndDateTimeInTheFuture() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(SPECIFIC_TIME_NAMED_DATE)
            .endDateTime(now().plusMinutes(1))
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWithInterimOrderWhenEndDateTimeNow() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(SPECIFIC_TIME_NAMED_DATE)
            .endDateTime(now())
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWithInterimOrderWhenEndDateTimeInThePast() {
        CaseDetails caseDetails = caseDetailsFor(INTERIM_ORDER_TYPE_AND_DOCUMENT, InterimEndDate.builder()
            .type(SPECIFIC_TIME_NAMED_DATE)
            .endDateTime(now().minusMinutes(1))
            .build());
        final AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "date-of-issue");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Enter an end date in the future");
    }

    @Test
    void shouldReturnErrorsWhenEPOTypeIsPreventRemovalButAddressIsIncomplete() {
        CaseDetails caseDetails = createCaseDetails(PREVENT_REMOVAL, now());
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "address");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce(
            "Enter a valid address for the contact",
            "Enter a postcode for the contact");
    }

    @Test
    void shouldNotReturnErrorsWhenEPOTypeIsRemoveToAccommodation() {
        CaseDetails caseDetails = createCaseDetails(REMOVE_TO_ACCOMMODATION, now());
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "address");
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenTheEPOEndDateIsNotWithinTheNextEightDays() {
        CaseData caseData = CaseData.builder().dateAndTimeOfIssue(now()).epoEndDate(now().plusDays(9)).build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "epo-end-date");
        assertThat(callbackResponse.getErrors()).containsOnlyOnce("Date must be within 8 days of the order date");
    }

    @Test
    void shouldNotReturnErrorsWhenTheEPOEndDateIsWithinTheNextEightDays() {
        CaseData caseData = CaseData.builder().dateAndTimeOfIssue(now()).epoEndDate(now().plusDays(7)).build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "epo-end-date");
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
        return createCaseDetails(preventRemoval, now, ORDER_TYPE_AND_DOCUMENT);
    }

    private CaseDetails createCaseDetails(EPOType epoType, LocalDateTime localDateTime,
                                          OrderTypeAndDocument orderTypeAndDocument) {

        return CaseDetails.builder()
            .data(Map.of(
                "dateOfIssue", localDateTime.toLocalDate(),
                "epoType", epoType,
                "epoRemovalAddress", Address.builder().build(),
                "epoEndDate", localDateTime,
                "orderTypeAndDocument", orderTypeAndDocument
            )).build();
    }

    private CaseDetails caseDetailsFor(OrderTypeAndDocument orderTypeAndDocument,
                                       InterimEndDate interimEndDate) {
        return CaseDetails.builder()
            .data(Map.of(
                "interimEndDate", interimEndDate,
                "orderTypeAndDocument", orderTypeAndDocument
            )).build();
    }
}
