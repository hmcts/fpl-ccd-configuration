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
        CaseData caseData = getCaseData(caseDetails);

        logAllQueryCollections(caseDetails);

        if (userService.isCafcassUser()) {
            String cafcassCollection = "qmCaseQueriesCollectionCafcass";

            log.info("Current logged-in user's case role is {}", UserRole.CAFCASS);
            log.info("Query collection for user role {} is {}.", UserRole.CAFCASS, cafcassCollection);
            caseDetails.getData().putIfAbsent(cafcassCollection, null);
            log.info("Setting {} to value {}", cafcassCollection, caseDetails.getData().get(cafcassCollection));
        } else {
            Set<CaseRole> currentUserRoles = userService.getCaseRoles(caseData.getId());
            log.info("Current logged-in user's case roles are: {}", currentUserRoles);
            initialiseRelevantQueryCollectionsForUser(caseDetails, currentUserRoles);
        }

        log.info("Final values of query collections: ");
        logAllQueryCollections(caseDetails);

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

    private void logAllQueryCollections(CaseDetails caseDetails) {
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionCafcass");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionLASol");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionEPSManaging");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionLAManaging");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionLABarrister");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionLAShared");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionBarrister");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitor");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorA");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorB");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorC");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorD");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorE");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorA");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorG");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorH");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorI");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionSolicitorJ");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolOne");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolTwo");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolThree");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolD");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolE");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolF");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolG");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolH");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolI");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolJ");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolK");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolL");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolM");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolN");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionChildSolO");
        logQueryCollection(caseDetails, "qmCaseQueriesCollectionCafcassSol");
    }

    private static Map<CaseRole, String> initialiseCollectionMapping() {
        Map<CaseRole, String> collectionMapping = new LinkedHashMap<>();
        collectionMapping.put(CaseRole.LASOLICITOR, "qmCaseQueriesCollectionLASol");
        collectionMapping.put(CaseRole.EPSMANAGING, "qmCaseQueriesCollectionEPSManaging");
        collectionMapping.put(CaseRole.LAMANAGING, "qmCaseQueriesCollectionLAManaging");
        collectionMapping.put(CaseRole.LABARRISTER, "qmCaseQueriesCollectionLABarrister");
        collectionMapping.put(CaseRole.LASHARED, "qmCaseQueriesCollectionLAShared");
        collectionMapping.put(CaseRole.BARRISTER, "qmCaseQueriesCollectionBarrister");
        collectionMapping.put(CaseRole.SOLICITOR, "qmCaseQueriesCollectionSolicitor");
        collectionMapping.put(CaseRole.SOLICITORA, "qmCaseQueriesCollectionSolicitorA");
        collectionMapping.put(CaseRole.SOLICITORB, "qmCaseQueriesCollectionSolicitorB");
        collectionMapping.put(CaseRole.SOLICITORC, "qmCaseQueriesCollectionSolicitorC");
        collectionMapping.put(CaseRole.SOLICITORD, "qmCaseQueriesCollectionSolicitorD");
        collectionMapping.put(CaseRole.SOLICITORE, "qmCaseQueriesCollectionSolicitorE");
        collectionMapping.put(CaseRole.SOLICITORF, "qmCaseQueriesCollectionSolicitorF");
        collectionMapping.put(CaseRole.SOLICITORG, "qmCaseQueriesCollectionSolicitorG");
        collectionMapping.put(CaseRole.SOLICITORH, "qmCaseQueriesCollectionSolicitorH");
        collectionMapping.put(CaseRole.SOLICITORI, "qmCaseQueriesCollectionSolicitorI");
        collectionMapping.put(CaseRole.SOLICITORJ, "qmCaseQueriesCollectionSolicitorJ");
        collectionMapping.put(CaseRole.CHILDSOLICITORA, "qmCaseQueriesCollectionChildSolOne");
        collectionMapping.put(CaseRole.CHILDSOLICITORB, "qmCaseQueriesCollectionChildSolTwo");
        collectionMapping.put(CaseRole.CHILDSOLICITORC, "qmCaseQueriesCollectionChildSolThree");
        collectionMapping.put(CaseRole.CHILDSOLICITORD, "qmCaseQueriesCollectionChildSolD");
        collectionMapping.put(CaseRole.CHILDSOLICITORE, "qmCaseQueriesCollectionChildSolE");
        collectionMapping.put(CaseRole.CHILDSOLICITORF, "qmCaseQueriesCollectionChildSolF");
        collectionMapping.put(CaseRole.CHILDSOLICITORG, "qmCaseQueriesCollectionChildSolG");
        collectionMapping.put(CaseRole.CHILDSOLICITORH, "qmCaseQueriesCollectionChildSolH");
        collectionMapping.put(CaseRole.CHILDSOLICITORI, "qmCaseQueriesCollectionChildSolI");
        collectionMapping.put(CaseRole.CHILDSOLICITORJ, "qmCaseQueriesCollectionChildSolJ");
        collectionMapping.put(CaseRole.CHILDSOLICITORK, "qmCaseQueriesCollectionChildSolK");
        collectionMapping.put(CaseRole.CHILDSOLICITORL, "qmCaseQueriesCollectionChildSolL");
        collectionMapping.put(CaseRole.CHILDSOLICITORM, "qmCaseQueriesCollectionChildSolM");
        collectionMapping.put(CaseRole.CHILDSOLICITORN, "qmCaseQueriesCollectionChildSolN");
        collectionMapping.put(CaseRole.CHILDSOLICITORO, "qmCaseQueriesCollectionChildSolO");
        collectionMapping.put(CaseRole.CAFCASSSOLICITOR, "qmCaseQueriesCollectionCafcassSol");
        return collectionMapping;
    }
}
