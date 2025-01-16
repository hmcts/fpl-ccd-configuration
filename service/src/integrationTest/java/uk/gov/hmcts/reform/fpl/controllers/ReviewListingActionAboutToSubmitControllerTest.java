package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.listing.ReviewListingActionController;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ReviewListingActionController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewListingActionAboutToSubmitControllerTest extends AbstractCallbackTest {

    public static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0);

    @MockBean
    private Time time;

    ReviewListingActionAboutToSubmitControllerTest() {
        super("review-listing-action");
    }

    @BeforeEach
    void beforeEach() {
        given(time.now()).willReturn(NOW);
    }

    @Test
    void shouldMoveListingRequestToReviewedCollection() {
        UUID uuid = randomUUID();
        CaseData before = CaseData.builder()
            .listingRequests(List.of(element(uuid, ListingActionRequest.builder()
                .type(List.of(ListingActionType.LISTING_REQUIRED))
                .details("List a new case management hearing in the future for an hour.")
                .dateSent(NOW.minusDays(1))
                .build())))
            .listingRequestsList(DynamicList.builder()
                .value(DynamicListElement.builder().code(uuid).build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(before);
        CaseData after = extractCaseData(response);

        assertThat(after.getListingRequests()).isEmpty();
        assertThat(after.getReviewedListingRequests()).hasSize(1);
        assertThat(after.getReviewedListingRequests().get(0).getValue())
            .isEqualTo(ListingActionRequest.builder()
                .type(List.of(ListingActionType.LISTING_REQUIRED))
                .details("List a new case management hearing in the future for an hour.")
                .dateSent(NOW.minusDays(1))
                .dateReviewed(NOW)
                .build());
    }

    @Test
    void shouldErrorIfNoActionFound() {
        UUID uuid = randomUUID();
        UUID wrongId = randomUUID();
        CaseData before = CaseData.builder()
            .listingRequests(List.of(element(uuid, ListingActionRequest.builder()
                .type(List.of(ListingActionType.LISTING_REQUIRED))
                .details("List a new case management hearing in the future for an hour.")
                .dateSent(NOW.minusDays(1))
                .build())))
            .listingRequestsList(DynamicList.builder()
                .value(DynamicListElement.builder().code(wrongId).build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(before);
        assertThat(response.getErrors()).containsExactly("Could not find listing request to review");
    }

}
