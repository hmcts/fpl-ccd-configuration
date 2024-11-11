package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ListingActionRequest;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListingActionService {

    private final Time time;

    public Map<String, Object> updateListingActions(CaseData caseData) {
        List<Element<ListingActionRequest>> listingRequests = Optional.ofNullable(caseData.getListingRequests())
            .orElse(new ArrayList<>());

        List<Element<ListingActionRequest>> reviewedListingRequests = Optional.ofNullable(
                caseData.getReviewedListingRequests())
            .orElse(new ArrayList<>());

        Optional<Element<ListingActionRequest>> requestElement = ElementUtils.findElement(
            caseData.getListingRequestsList().getValueCodeAsUUID(), listingRequests);

        if (requestElement.isPresent()) {
            Element<ListingActionRequest> req = requestElement.get();
            req.setValue(req.getValue().toBuilder()
                .dateReviewed(time.now())
                .build());

            listingRequests = ElementUtils.removeElementWithUUID(listingRequests, req.getId());
            reviewedListingRequests.add(0, req);
        } else {
            throw new IllegalArgumentException("Could not find listing request to review");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("listingRequests", listingRequests);
        updates.put("reviewedListingRequests", reviewedListingRequests);

        return updates;
    }
}
