package uk.gov.hmcts.reform.fpl.controllers.listing;


import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@RestController
@RequestMapping("/callback/request-listing-action")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestListingActionController extends CallbackController {

    private final Time time;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().remove("lastListingRequestType");
        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<ListingActionRequest>> listingRequests = Optional.ofNullable(caseData.getListingRequests())
            .orElse(new ArrayList<>());

        ListingActionRequest newRequest = ListingActionRequest.builder()
            .type(caseData.getSelectListingActions())
            .details(caseData.getListingDetails())
            .dateSent(time.now())
            .build();

        listingRequests.add(0, element(newRequest));

        caseDetails.getData().put("listingRequests", listingRequests);
        caseDetails.getData().put("lastListingRequestType", newRequest.getTypesLabel().replace(", ", ";"));

        removeTemporaryFields(caseDetails, "selectListingActions", "listingDetails");
        return respond(caseDetails);
    }

}
