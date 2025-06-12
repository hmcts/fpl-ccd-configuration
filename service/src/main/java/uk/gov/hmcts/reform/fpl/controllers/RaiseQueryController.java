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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getCurrentCollectionByUserService;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getLatestQueryIDForCollection;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getRoleToCollectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.logAllQueryCollections;

@Slf4j
@RestController
@RequestMapping("/callback/raise-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RaiseQueryController extends CallbackController {

    private final UserService userService;
    private static final Map<CaseRole, String> COLLECTION_MAPPING = getRoleToCollectionMapping();

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        String queryCollection;

        logAllQueryCollections(caseDetails);

        if (userService.isCafcassUser()) {
            queryCollection = "qmCaseQueriesCollectionCafcass";
            log.info("Current logged-in user's case role is {}", UserRole.CAFCASS);
        } else {
            queryCollection = getCurrentCollectionByUserService(caseData, userService, COLLECTION_MAPPING);
        }

        log.info("Query collection for this user is {}.", queryCollection);
        caseDetails.getData().putIfAbsent(queryCollection, null);
        log.info("Setting {} to value {}", queryCollection, caseDetails.getData().get(queryCollection));

        log.info("Final values of query collections: ");
        logAllQueryCollections(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseDetails.getData().put("latestQueryID", getLatestQueryIDForCollection(caseDetails,
            getCurrentCollectionByUserService(caseData, userService, COLLECTION_MAPPING)));

        log.info("latestQueryID is set to: {}", caseDetails.getData().get("latestQueryID"));

        return respond(caseDetails);
    }
}
