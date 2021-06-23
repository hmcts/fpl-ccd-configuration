package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final CaseSubmissionService caseSubmissionService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3093".equals(migrationId)) {
            run3093(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3093(CaseDetails caseDetails) {
        final String familyManCaseNumber = "KH21C50008";
        final UUID additionalApplicationBundleId = UUID.fromString("c7b47c00-4b7a-4dd8-8bce-140e41ab4bb0");
        final UUID c2ApplicationId = UUID.fromString("30d385b9-bdc5-4145-aeb5-ffee5afd1f02");

        CaseData caseData = getCaseData(caseDetails);

        if (!Objects.equals(familyManCaseNumber, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(String.format(
                "Migration FPLA-3093: Expected family man case number to be %s but was %s",
                familyManCaseNumber, caseData.getFamilyManCaseNumber()));
        }

        List<Element<AdditionalApplicationsBundle>> bundles = caseData.getAdditionalApplicationsBundle();

        validateAdditionalApplicationsBundles(additionalApplicationBundleId, c2ApplicationId, bundles);

        bundles.removeIf(bundle -> additionalApplicationBundleId.equals(bundle.getId()));
        caseDetails.getData().put("additionalApplicationsBundle", bundles);
    }

    private void validateAdditionalApplicationsBundles(UUID additionalApplicationBundleId,
                                                       UUID c2ApplicationId,
                                                       List<Element<AdditionalApplicationsBundle>> bundles) {

        Element<AdditionalApplicationsBundle> bundleElement = bundles.stream()
            .filter(bundle -> additionalApplicationBundleId.equals(bundle.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError(String.format(
                "Migration FPLA-3093: Expected additional application bundle id to be %s but not found",
                additionalApplicationBundleId
            )));

        C2DocumentBundle c2DocumentBundle = bundleElement.getValue().getC2DocumentBundle();

        if (!Objects.equals(c2ApplicationId, c2DocumentBundle.getId())) {
            throw new AssertionError(String.format(
                "Migration FPLA-3093: Expected c2 bundle Id to be %s but not found", c2ApplicationId
            ));
        }
    }

}
