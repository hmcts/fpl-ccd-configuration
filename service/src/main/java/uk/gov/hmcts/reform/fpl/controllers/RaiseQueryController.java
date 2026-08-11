package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.QueryManagementService;
import uk.gov.hmcts.reform.fpl.service.UserService;

@Slf4j
@RestController
@RequestMapping("/callback/raise-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RaiseQueryController extends CallbackController {

    private final UserService userService;
    private final QueryManagementService queryManagementService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        String queryCollection;

        queryManagementService.logAllQueryCollections(caseDetails);

        if (userService.isCafcassUser()) {
            queryCollection = "qmCaseQueriesCollectionCafcass";
            log.info("Current logged-in user's case role is {}", UserRole.CAFCASS);
        } else {
            queryCollection = queryManagementService.getCurrentCollectionByLoggedInUserRole(caseData);
        }

        log.info("Query collection for this user is {}.", queryCollection);
        caseDetails.getData().putIfAbsent(queryCollection, null);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseDetails.getData().put("latestQueryID", queryManagementService.getLatestQueryIDForCollection(caseDetails,
            queryManagementService.getCurrentCollectionByLoggedInUserRole(caseData)));

        return respond(caseDetails);
    }
}
