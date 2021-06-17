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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3088".equals(migrationId)) {
            run3088(caseDetails);
        }

        if ("FPLA-3089".equals(migrationId)) {
            run3089(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3088(CaseDetails caseDetails) {
        final String familyManCaseNumber = "CF21C50022";
        final UUID additionalApplicationBundleId = UUID.fromString("1ccca4f7-40d5-4392-a199-ae9372f53d00");
        final UUID c2ApplicationId = UUID.fromString("e3d5bac0-4ba6-48b6-b6d5-e60d5234a183");

        removeAdditionalApplicationBundle(caseDetails,
            familyManCaseNumber,
            additionalApplicationBundleId,
            c2ApplicationId,
            "FPLA-3088");
    }

    private void run3089(CaseDetails caseDetails) {
        final String familyManCaseNumber = "WR21C50042";
        final UUID additionalApplicationBundleId = UUID.fromString("6dc62c7b-47a5-4e26-8ce3-99a697f72454");
        final UUID c2ApplicationId = UUID.fromString("2b2159aa-6ab8-4b63-b65b-08cb896de2ec");

        removeAdditionalApplicationBundle(caseDetails,
            familyManCaseNumber,
            additionalApplicationBundleId,
            c2ApplicationId,
            "FPLA-3089");
    }

    private void removeAdditionalApplicationBundle(CaseDetails caseDetails,
                                                   String familyManCaseNumber,
                                                   UUID additionalApplicationBundleId,
                                                   UUID c2ApplicationId,
                                                   String migrationId) {
        CaseData caseData = getCaseData(caseDetails);

        if (!Objects.equals(familyManCaseNumber, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(String.format(
                "Migration %s: Expected family man case number to be %s but was %s",
                migrationId, familyManCaseNumber, caseData.getFamilyManCaseNumber()));
        }

        List<Element<AdditionalApplicationsBundle>> bundles = caseData.getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> additionalApplicationsBundleElement = bundles.stream()
            .filter(bundle -> additionalApplicationBundleId.equals(bundle.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError(String.format(
                "Migration %s: Expected additional application bundle id to be %s but not found",
                migrationId, additionalApplicationBundleId
            )));

        validateBundle(additionalApplicationsBundleElement.getValue(), c2ApplicationId, migrationId);

        bundles.removeIf(bundle -> additionalApplicationBundleId.equals(bundle.getId()));

        caseDetails.getData().put("additionalApplicationsBundle", bundles);
    }

    private void validateBundle(AdditionalApplicationsBundle additionalApplicationsBundle,
                                UUID c2BundleId,
                                String migrationId) {
        C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();

        if (!Objects.equals(c2BundleId, c2DocumentBundle.getId())) {
            throw new AssertionError(String.format(
                "Migration %s: Expected c2 bundle Id to be %s but not found", migrationId, c2BundleId
            ));
        }

        if (!isNull(additionalApplicationsBundle.getOtherApplicationsBundle())) {
            throw new AssertionError(
                String.format("Migration %s: Unexpected other application bundle", migrationId));
        }
    }

}
