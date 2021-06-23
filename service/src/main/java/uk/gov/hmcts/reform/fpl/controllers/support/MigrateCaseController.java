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
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final DocumentListService documentListService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3093".equals(migrationId)) {
            run3093(caseDetails);
        }

        if ("FPLA-3175".equals(migrationId)) {
            run3175(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3175(CaseDetails caseDetails) {
        final String familyManCaseNumber = "CV21C50026";

        Map<UUID, String> documentsToRemove = Map.of(
            UUID.fromString("339ad24a-e83c-4b81-9743-46c7771b3975"),
            "Children's Guardian Position Statement 22 .06.2021",

            UUID.fromString("dc8ad08b-e7ba-4075-a9e4-0ff8fc85616b"),
            "Supporting documents for Children's Guardian Position Statement no 1",

            UUID.fromString("008f3053-d949-4aaa-9d65-1027e88ed288"),
            "Supporting documents for Children's Guardian Position Statement no 2",

            UUID.fromString("671d6805-c042-4b3e-88e8-7c7fd103a026"),
            "Supporting documents for Children's Guardian Position Statement no 3",

            UUID.fromString("74c918f2-6409-4651-bb7b-1f58c39aee94"),
            "Supporting documents for Children's Guardian Position Statement no 4",

            UUID.fromString("03343373-df27-4744-a63c-56a98442662a"),
            "Supporting documents for Children's Guardian Position Statement no 5"
        );

        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3175", familyManCaseNumber, caseData);

        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();

        for (Map.Entry<UUID, String> entry : documentsToRemove.entrySet()) {
            removeOtherCourtAdminDocument(otherCourtAdminDocuments, entry);
        }
        caseDetails.getData().put("otherCourtAdminDocuments", otherCourtAdminDocuments);
        caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(caseDetails)));
    }

    private void removeOtherCourtAdminDocument(List<Element<CourtAdminDocument>> otherCourtAdminDocuments,
                                               Map.Entry<UUID, String> documentToRemove) {
        if (otherCourtAdminDocuments.stream().noneMatch(document -> documentToRemove.getKey().equals(document.getId())
            && documentToRemove.getValue().equals(document.getValue().getDocumentTitle()))) {

            throw new AssertionError(String.format(
                "Migration FPLA-3175: Expected other court admin document Id %s and document title '%s' "
                    + "but not found", documentToRemove.getKey(), documentToRemove.getValue()
            ));
        }

        otherCourtAdminDocuments.removeIf(document -> documentToRemove.getKey().equals(document.getId())
            && documentToRemove.getValue().equals(document.getValue().getDocumentTitle()));
    }

    private void run3093(CaseDetails caseDetails) {
        final String familyManCaseNumber = "KH21C50008";
        final UUID additionalApplicationBundleId = UUID.fromString("c7b47c00-4b7a-4dd8-8bce-140e41ab4bb0");
        final UUID c2ApplicationId = UUID.fromString("30d385b9-bdc5-4145-aeb5-ffee5afd1f02");

        CaseData caseData = getCaseData(caseDetails);

        validateFamilyManNumber("FPLA-3093", familyManCaseNumber, caseData);

        List<Element<AdditionalApplicationsBundle>> bundles = caseData.getAdditionalApplicationsBundle();

        validateAdditionalApplicationsBundles(additionalApplicationBundleId, c2ApplicationId, bundles);

        bundles.removeIf(bundle -> additionalApplicationBundleId.equals(bundle.getId()));
        caseDetails.getData().put("additionalApplicationsBundle", bundles);
    }

    private void validateFamilyManNumber(String migrationId, String familyManCaseNumber, CaseData caseData) {
        if (!Objects.equals(familyManCaseNumber, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(String.format(
                "Migration %s: Expected family man case number to be %s but was %s",
                migrationId, familyManCaseNumber, caseData.getFamilyManCaseNumber()));
        }
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
