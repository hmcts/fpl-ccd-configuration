package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.listing.RequestListingActionController;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(RequestListingActionController.class)
@OverrideAutoConfiguration(enabled = true)
class RequestListingActionAboutToSubmitControllerTest extends AbstractCallbackTest {

    public static final String LISTING_DETAILS = "List a new case management hearing in the future for an hour.";
    public static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0);

    @MockBean
    private Time time;

    RequestListingActionAboutToSubmitControllerTest() {
        super("request-listing-action");
    }

    @BeforeEach
    void beforeEach() {
        given(time.now()).willReturn(NOW);
    }

    @Test
    void shouldAddAFirstRequestToTheCollection() {
        CaseData caseData = CaseData.builder()
            .selectListingActions(List.of(ListingActionType.LISTING_REQUIRED))
            .listingDetails(LISTING_DETAILS)
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(caseData);

        CaseData actualCaseData = mapper.convertValue(actualResponse.getData(), CaseData.class);

        assertThat(actualCaseData.getListingRequests()).hasSize(1);
        assertThat(actualCaseData.getListingRequests().get(0).getValue())
            .isEqualTo(ListingActionRequest.builder()
                .type(List.of(ListingActionType.LISTING_REQUIRED))
                .details(LISTING_DETAILS)
                .dateSent(NOW)
                .build());
    }

    @Test
    void shouldAddAFollowUpRequestToTheCollection() {
        ListingActionRequest oldRequest = ListingActionRequest.builder()
            .type(List.of(ListingActionType.LISTING_REQUIRED))
            .details(LISTING_DETAILS)
            .dateSent(NOW.minusDays(1))
            .build();

        CaseData caseData = CaseData.builder()
            .selectListingActions(List.of(ListingActionType.SPECIAL_MEASURES_REQUIRED))
            .listingDetails("second details")
            .listingRequests(List.of(element(oldRequest)))
            .build();

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(caseData);

        CaseData actualCaseData = mapper.convertValue(actualResponse.getData(), CaseData.class);

        ListingActionRequest expectedNewRequest = ListingActionRequest.builder()
            .type(List.of(ListingActionType.SPECIAL_MEASURES_REQUIRED))
            .details("second details")
            .dateSent(NOW)
            .build();

        assertThat(actualCaseData.getListingRequests().stream().map(Element::getValue).toList())
            .containsExactly(expectedNewRequest, oldRequest);
    }

}
