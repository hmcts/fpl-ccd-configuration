package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.RespondQueryEvent;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getParentQueryFromResponse;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getQueryDateFromQuery;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getQueryResponseFromCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getUserIdFromQuery;

@Slf4j
@RestController
@RequestMapping("/callback/respond-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondQueryController extends CallbackController {

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        log.info("Going to send notification");

        Map<String,Object> queryResponse = getQueryResponseFromCaseDetails(callbackRequest.getCaseDetailsBefore(),
            callbackRequest.getCaseDetails());

        log.info("Query response: {}", queryResponse); //For debugging purposes

        Map<String,Object> parentQuery = getParentQueryFromResponse(callbackRequest.getCaseDetails(), queryResponse);

        log.info("Parent query: {}", parentQuery); //For debugging purposes

        publishEvent(new RespondQueryEvent(
            getCaseData(callbackRequest.getCaseDetails()),
            getUserIdFromQuery(parentQuery),
            getQueryDateFromQuery(parentQuery))
        );
    }
}
