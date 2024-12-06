package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.listing.ReviewListingActionController;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReviewListingActionController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewListingActionMidEventControllerTest extends AbstractCallbackTest {

    public static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0);

    ReviewListingActionMidEventControllerTest() {
        super("review-listing-action");
    }

    @Test
    void shouldAddListingRequestDetailsToEventJourney() {
        UUID uuid = randomUUID();
        ListingActionRequest listingActionRequest = ListingActionRequest.builder()
            .type(List.of(ListingActionType.LISTING_REQUIRED))
            .details("List a new case management hearing in the future for an hour.")
            .dateSent(NOW)
            .build();

        CaseData before = CaseData.builder()
            .listingRequests(List.of(element(uuid, listingActionRequest)))
            .listingRequestsList(DynamicList.builder()
                .value(DynamicListElement.builder().code(uuid).build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(before);
        CaseData after = extractCaseData(response);

        assertThat(after.getListingRequestToReview()).isEqualTo(listingActionRequest);
    }

    @Test
    void shouldErrorIfNoActionFound() {
        CaseData before = CaseData.builder()
            .listingRequests(List.of())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(before);
        assertThat(response.getErrors()).containsExactly("There are no listing actions to review.");
    }

}
