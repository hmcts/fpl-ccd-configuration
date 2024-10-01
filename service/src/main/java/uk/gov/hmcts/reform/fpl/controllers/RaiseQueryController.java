package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
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

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/callback/raise-query")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RaiseQueryController extends CallbackController {

    private final UserService userService;

    private static final Map<CaseRole, String> COLLECTION_MAPPING = initialiseUserTypeToQMCollectionMapping();

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Set<CaseRole> currentUserRoles = userService.getCaseRoles(caseData.getId());

        for (CaseRole user : currentUserRoles) {
            if (isNull(caseDetails.getData().getOrDefault(COLLECTION_MAPPING.get(user), null))) {
                caseDetails.getData().put(COLLECTION_MAPPING.get(user), null);
            }
        }

        return respond(caseDetails);
    }

    private static Map<CaseRole, String> initialiseUserTypeToQMCollectionMapping() {
        Map<CaseRole, String> collectionMapping = new LinkedHashMap<>();

        collectionMapping.put(CaseRole.CHILDSOLICITORA, "qmCaseQueriesCollectionChildSolA");
        collectionMapping.put(CaseRole.CHILDSOLICITORB, "qmCaseQueriesCollectionChildSolB");

        return collectionMapping;
    }
}
