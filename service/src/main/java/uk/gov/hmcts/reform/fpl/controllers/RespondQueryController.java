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
import uk.gov.hmcts.reform.fpl.service.QueryManagementService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/callback/respond-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondQueryController extends CallbackController {

    private final QueryManagementService queryManagementService;

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        log.info("Going to send notification");

        Map<String,Object> queryResponse = queryManagementService.getQueryResponseFromCaseDetails(callbackRequest.getCaseDetailsBefore(),
            callbackRequest.getCaseDetails());

        Map<String,Object> parentQuery = queryManagementService.getParentQueryFromResponse(callbackRequest.getCaseDetails(), queryResponse);

        publishEvent(new RespondQueryEvent(
            getCaseData(callbackRequest.getCaseDetails()),
            queryManagementService.getUserIdFromQuery(parentQuery),
            queryManagementService.getQueryDateFromQuery(parentQuery))
        );
    }
}
