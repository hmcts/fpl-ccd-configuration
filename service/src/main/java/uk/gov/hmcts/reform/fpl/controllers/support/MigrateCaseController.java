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
import java.util.Map;
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

        if ("FPLA-3037".equals(migrationId)) {
            run3037(caseDetails);
        }

        if ("FPLA-3093".equals(migrationId)) {
            run3093(caseDetails);
        }

        if ("FPLA-2991".equals(migrationId)) {
            run2991(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3037(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles =
            caseData.getAdditionalApplicationsBundle();

        additionalApplicationsBundles.forEach(
            additionalApplicationsBundle -> {
                C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getValue().getC2DocumentBundle();

                c2DocumentBundle.getSupportingEvidenceBundle()
                    .removeIf(supportingEvidenceBundle -> supportingEvidenceBundle.getId()
                        .toString()
                        .equals("4885a0e2-fd88-4614-9c35-6c61d6b5e422")
                    );
            }
        );

        data.put("additionalApplicationsBundle", additionalApplicationsBundles);
        caseDetails.setData(data);
    }

    private void run3093(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = caseData
            .getAdditionalApplicationsBundle();

        if (caseData.getAdditionalApplicationsBundle().size() < 1) {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Case has %s additional applications",
                    caseData.getFamilyManCaseNumber(), additionalApplicationsBundle.size()));
        }

        additionalApplicationsBundle.remove(0);

        Map<String, Object> data = caseDetails.getData();

        data.put("additionalApplicationsBundle", additionalApplicationsBundle);
    }

    private void run2991(CaseDetails caseDetails) {
        final UUID secondBundleID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
        final UUID supportingEvidenceID = UUID.fromString("045c1fd6-3fed-42d3-be0b-e47257f6c01c");

        CaseData caseData = getCaseData(caseDetails);

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = caseData
            .getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> secondBundle = additionalApplicationsBundle.get(1);
        if (secondBundle.getId().equals(secondBundleID)) {
            C2DocumentBundle c2DocumentBundle = secondBundle.getValue().getC2DocumentBundle();

            c2DocumentBundle.getSupportingEvidenceBundle()
                .removeIf(bundle -> bundle.getId()
                    .equals(supportingEvidenceID)
                );

        } else {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected %s but got %s",
                    caseData.getFamilyManCaseNumber(), secondBundleID, secondBundle.getId()));
        }

        Map<String, Object> data = caseDetails.getData();
        data.put("additionalApplicationsBundle", additionalApplicationsBundle);
    }
}
