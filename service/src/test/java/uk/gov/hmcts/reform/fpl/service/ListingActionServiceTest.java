package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class ListingActionServiceTest {

    public static final LocalDateTime REVIEW_DATE = LocalDateTime.of(2020, 1, 2, 0, 0);
    @Mock
    private Time time;

    @InjectMocks
    private ListingActionService underTest;

    @Test
    void shouldRemoveFromRequestsListAndAddToReviewedList() {
        when(time.now()).thenReturn(REVIEW_DATE);

        UUID reqId = UUID.randomUUID();
        ListingActionRequest request = ListingActionRequest.builder()
            .details("Listing required on a day in the future")
            .dateSent(LocalDateTime.of(2020, 1, 1, 0, 0))
            .type(List.of(ListingActionType.LISTING_REQUIRED))
            .build();

        CaseData caseData = CaseData.builder()
            .listingRequests(List.of(element(reqId, request)))
            .listingRequestsList(DynamicList.builder()
                .value(DynamicListElement.builder().code(reqId).build())
                .build())
            .build();

        Map<String, Object> updates = underTest.updateListingActions(caseData);

        ListingActionRequest expectedReviewedRequest = request.toBuilder()
            .dateReviewed(REVIEW_DATE)
            .build();

        assertThat(updates).isEqualTo(Map.of(
            "listingRequests", List.of(),
            "reviewedListingRequests", List.of(element(reqId, expectedReviewedRequest))
        ));
    }

    @Test
    void shouldThrowExceptionIfNotValidListingRequest() {
        UUID reqId = UUID.randomUUID();
        ListingActionRequest request = ListingActionRequest.builder()
            .details("Listing required on a day in the future")
            .dateSent(LocalDateTime.of(2020, 1, 1, 0, 0))
            .type(List.of(ListingActionType.LISTING_REQUIRED))
            .build();

        CaseData caseData = CaseData.builder()
            .listingRequests(List.of(element(reqId, request)))
            .listingRequestsList(DynamicList.builder()
                .value(DynamicListElement.builder().code(UUID.randomUUID()).build())
                .build())
            .build();

        assertThatThrownBy(() -> underTest.updateListingActions(caseData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Could not find listing request to review");
    }
}
