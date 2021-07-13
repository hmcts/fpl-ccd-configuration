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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final DocumentListService documentListService;
    private final ManageDocumentService manageDocumentService;
    private final ConfidentialDocumentsSplitter confidentialDocumentsSplitter;
    private final CaseAccessService caseAccessService;

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseData = getCaseData(callbackRequest);

        Set<String> users = Set.of(
            "573d3000-d4a4-4532-941d-93534f887acd",
            "5bd3491b-aca1-493b-999e-d770604abe83",
            "cc764c80-02a3-4cd5-9f8f-94f11658a7fb",
            "f4b9d49f-b3ab-467a-94ff-afb758dea71e",
            "5655587e-2878-4934-a9ff-0cbd4e354327",
            "777f7d7f-5dd4-4c92-af60-35a6a98086c8",
            "6d839a65-0cda-4c59-96ec-92efd63e0c2e",
            "9eb5184b-dbfb-445b-9520-14bd5c6b7e09"
        );

        final Object oldLA = caseDetailsBefore.getData().get("caseLocalAuthority");
        final Object newLA = caseDetails.getData().get("caseLocalAuthority");

        if ("PO21C50011".equals(caseData.getFamilyManCaseNumber()) && "SCC".equals(oldLA) && "BCP".equals(newLA)) {
            caseAccessService.grantCaseRoleToUsers(caseData.getId(), users, CaseRole.LASOLICITOR);
        }

    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2486".equals(migrationId)) {
            run2486(caseDetails);
        }

        if ("FPLA-3175".equals(migrationId)) {
            run3175(caseDetails);
        }

        if ("FPLA-3226".equals(migrationId)) {
            run3226(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3226(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("PO21C50011".equals(caseData.getFamilyManCaseNumber())) {

            if (!"SCC".equals(caseData.getCaseLocalAuthority())) {
                throw new IllegalStateException(
                    String.format("Expected local authority SCC, but got %s", caseData.getCaseLocalAuthority()));
            }

            caseDetails.getData().put("caseLocalAuthority", "BCP");
            caseDetails.getData().put("caseLocalAuthorityName", "Bournemouth, Christchurch and Poole Council");
            caseDetails.getData().put("localAuthorityPolicy", organisationPolicy(
                "NAXQHHD", "Bournemouth, Christchurch and Poole Council", CaseRole.LASOLICITOR));
        } else {
            throw new IllegalStateException("Unexpected FMN: " + caseData.getFamilyManCaseNumber());
        }
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

            throw new AssertionError(format(
                "Migration FPLA-3175: Expected other court admin document Id %s and document title '%s' "
                    + "but not found", documentToRemove.getKey(), documentToRemove.getValue()
            ));
        }

        otherCourtAdminDocuments.removeIf(document -> documentToRemove.getKey().equals(document.getId())
            && documentToRemove.getValue().equals(document.getValue().getDocumentTitle()));
    }

    private void run2486(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isNotEmpty(caseData.getCorrespondenceDocuments())) {
            log.info("Migration FPLA-2486: Updating correspondence documents for case reference: {}",
                caseDetails.getId());
            sortCorrespondenceDocuments(
                caseData.getCorrespondenceDocuments(), caseDetails, CORRESPONDING_DOCUMENTS_COLLECTION_KEY);
        }

        if (isNotEmpty(caseData.getCorrespondenceDocumentsLA())) {
            log.info("Migration FPLA-2486: Updating LA correspondence documents for case reference: {}",
                caseDetails.getId());
            sortCorrespondenceDocuments(
                caseData.getCorrespondenceDocumentsLA(), caseDetails, CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY);
        }
    }

    private void sortCorrespondenceDocuments(List<Element<SupportingEvidenceBundle>> correspondenceDocuments,
                                             CaseDetails caseDetails,
                                             String fieldName) {
        List<Element<SupportingEvidenceBundle>> sortedDocuments
            = manageDocumentService.sortCorrespondenceDocumentsByUploadedDate(correspondenceDocuments);
        caseDetails.getData().put(fieldName, sortedDocuments);

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        confidentialDocumentsSplitter.updateConfidentialDocsInCaseDetails(caseDetailsMap, sortedDocuments, fieldName);
        caseDetails.getData().put(fieldName + "NC", caseDetailsMap.get(fieldName + "NC"));
    }

    private void validateFamilyManNumber(String migrationId, String familyManCaseNumber, CaseData caseData) {
        if (!Objects.equals(familyManCaseNumber, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(format(
                "Migration %s: Expected family man case number to be %s but was %s",
                migrationId, familyManCaseNumber, caseData.getFamilyManCaseNumber()));
        }
    }

}
