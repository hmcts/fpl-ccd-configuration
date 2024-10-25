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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/callback/raise-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RaiseQueryController extends CallbackController {

    private final UserService userService;
    private static final Map<CaseRole, String> COLLECTION_MAPPING = initialiseCollectionMapping();

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolOne");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolTwo");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolThree");

        CaseData caseData = getCaseData(caseDetails);

        Set<CaseRole> currentUserRoles = userService.getCaseRoles(caseData.getId());
        log.info("Current logged-in user's case roles are: {}", currentUserRoles);

        initialiseRelevantQueryCollectionsForUser(caseDetails, currentUserRoles);

        log.info("Final values of query collections: ");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolOne");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolTwo");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolThree");

        return respond(caseDetails);
    }

    private void initialiseRelevantQueryCollectionsForUser(CaseDetails caseDetails, Set<CaseRole> currentUserRoles) {
        CaseRole userQueryCollectionRole = currentUserRoles.stream()
            .filter(COLLECTION_MAPPING::containsKey)
            .findFirst()
            .orElse(null);

        if (userQueryCollectionRole != null) {
            String queryCollectionKey = COLLECTION_MAPPING.get(userQueryCollectionRole);
            log.info("Query collection for user role {} is {}.", userQueryCollectionRole, queryCollectionKey);
            caseDetails.getData().putIfAbsent(queryCollectionKey, null);
            log.info("Setting {} to value {}", queryCollectionKey, caseDetails.getData().get(queryCollectionKey));
        }
    }

    private void logQueryCollection(CaseDetails caseDetails, String collectionKey) {
        if (caseDetails.getData().containsKey(collectionKey)) {
            log.info("{} is present with value: {}", collectionKey, caseDetails.getData().get(collectionKey));
        } else {
            log.info("{} is not present on case data.", collectionKey);
        }
    }

    private static Map<CaseRole, String> initialiseCollectionMapping() {
        Map<CaseRole, String> collectionMapping = new LinkedHashMap<>();
        collectionMapping.put(CaseRole.CHILDSOLICITORA, "qmCaseQueriesCollectionChildSolOne");
        collectionMapping.put(CaseRole.CHILDSOLICITORB, "qmCaseQueriesCollectionChildSolTwo");
        collectionMapping.put(CaseRole.CHILDSOLICITORC, "qmCaseQueriesCollectionChildSolThree");
        return collectionMapping;
    }
}
