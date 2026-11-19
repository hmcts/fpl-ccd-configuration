package uk.gov.hmcts.reform.fpl.controllers.support;

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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService;

@Slf4j
@RestController
@RequestMapping("/callback/case-flags")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsMigrationController extends CallbackController {

    private final CaseFlagsService caseFlagsService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/migration/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse migrateCaseFlags(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (featureToggleService.isCaseFlagsV2Enabled()) {
            CaseData caseData = getCaseData(caseDetails);
            caseFlagsService.migrateCaseFlags(caseData);
            caseDetails.getData().putAll(caseFlagsService.generate(caseData));
            log.info("Case Flags v2.1 migration completed for case {}", caseDetails.getId());
        } else {
            log.info("Case Flags v2.1 migration skipped for case {} because the feature is disabled",
                caseDetails.getId());
        }

        return respond(caseDetails);
    }

    @PostMapping("/rollback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse rollbackCaseFlags(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (featureToggleService.isCaseFlagsV2Enabled()) {
            CaseData caseData = getCaseData(caseDetails);
            caseFlagsService.rollbackCaseFlags(caseData);
            caseDetails.getData().putAll(caseFlagsService.generateRollback());
            log.info("Case Flags v2.1 rollback completed for case {}", caseDetails.getId());
        } else {
            log.info("Case Flags v2.1 rollback skipped for case {} because the feature is disabled",
                caseDetails.getId());
        }

        return respond(caseDetails);
    }
}
