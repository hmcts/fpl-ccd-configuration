package uk.gov.hmcts.reform.fpl.controllers.listing;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ListingActionService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Slf4j
@RestController
@RequestMapping("/callback/review-listing-action")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewListingActionController extends CallbackController {

    private final ListingActionService listingActionService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getListingRequests())) {
            return respond(caseDetails, List.of("There are no listing actions to review."));
        }

        caseDetails.getData().put("listingRequestsList",
            asDynamicList(caseData.getListingRequests(), ListingActionRequest::toLabel));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getListingRequests())) {
            return respond(caseDetails, List.of("There are no listing actions to review."));
        }

        Optional<Element<ListingActionRequest>> requestElement = ElementUtils.findElement(
            caseData.getListingRequestsList().getValueCodeAsUUID(), caseData.getListingRequests());

        if (requestElement.isEmpty()) {
            return respond(caseDetails, List.of("No listing action selected"));
        }

        caseDetails.getData().put("listingRequestToReview", requestElement.get().getValue());

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        try {
            caseDetails.getData().putAll(listingActionService.updateListingActions(caseData));
            removeTemporaryFields(caseDetails,
                "listingRequestsList", "listingRequestToReview", "markListingActionReviewed");
        } catch (IllegalArgumentException ex) {
            return respond(caseDetails, List.of(ex.getMessage()));
        }
        return respond(caseDetails);
    }

}
