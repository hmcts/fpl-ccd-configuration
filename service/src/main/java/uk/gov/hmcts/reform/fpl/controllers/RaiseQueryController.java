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
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/callback/raise-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RaiseQueryController extends CallbackController {

    private static final List<String> QUERY_COLLECTIONS = List.of(
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionLASol",
        "qmCaseQueriesCollectionEPSManaging",
        "qmCaseQueriesCollectionLAManaging",
        "qmCaseQueriesCollectionLABarrister",
        "qmCaseQueriesCollectionLAShared",
        "qmCaseQueriesCollectionBarrister",
        "qmCaseQueriesCollectionSolicitor",
        "qmCaseQueriesCollectionSolicitorA",
        "qmCaseQueriesCollectionSolicitorB",
        "qmCaseQueriesCollectionSolicitorC",
        "qmCaseQueriesCollectionSolicitorD",
        "qmCaseQueriesCollectionSolicitorE",
        "qmCaseQueriesCollectionSolicitorF",
        "qmCaseQueriesCollectionSolicitorG",
        "qmCaseQueriesCollectionSolicitorH",
        "qmCaseQueriesCollectionSolicitorI",
        "qmCaseQueriesCollectionSolicitorJ",
        "qmCaseQueriesCollectionChildSolA",
        "qmCaseQueriesCollectionChildSolB",
        "qmCaseQueriesCollectionChildSolC",
        "qmCaseQueriesCollectionChildSolD",
        "qmCaseQueriesCollectionChildSolE",
        "qmCaseQueriesCollectionChildSolF",
        "qmCaseQueriesCollectionChildSolG",
        "qmCaseQueriesCollectionChildSolH",
        "qmCaseQueriesCollectionChildSolI",
        "qmCaseQueriesCollectionChildSolJ",
        "qmCaseQueriesCollectionChildSolK",
        "qmCaseQueriesCollectionChildSolL",
        "qmCaseQueriesCollectionChildSolM",
        "qmCaseQueriesCollectionChildSolN",
        "qmCaseQueriesCollectionChildSolO",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionCafcassSol"
    );

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        logAllCollections(caseDetails, QUERY_COLLECTIONS);

        for (String queryCollection : QUERY_COLLECTIONS) {
            caseDetails.getData().putIfAbsent(queryCollection, null);
            log.info("Setting {} to value {}", queryCollection, caseDetails.getData().get(queryCollection));
        }

        log.info("Final values of query collections: ");
        logAllCollections(caseDetails, QUERY_COLLECTIONS);

        return respond(caseDetails);
    }

    private void logQueryCollection(CaseDetails caseDetails, String collectionKey) {
        if (caseDetails.getData().containsKey(collectionKey)) {
            log.info("{} is present with value: {}", collectionKey, caseDetails.getData().get(collectionKey));
        } else {
            log.info("{} is not present on case data.", collectionKey);
        }
    }

    private void logAllCollections(CaseDetails caseDetails, List<String> collections) {
        for (String collectionKey : collections) {
            logQueryCollection(caseDetails, collectionKey);
        }
    }
}
