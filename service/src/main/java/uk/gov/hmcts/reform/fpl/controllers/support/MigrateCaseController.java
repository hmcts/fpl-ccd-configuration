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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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

        if ("FPLA-3214".equals(migrationId)) {
            run3214(caseDetails);
        }

        if ("FPLA-3239".equals(migrationId)) {
            run3239(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3214(CaseDetails caseDetails) {
        if (isNotEmpty(caseDetails.getData().get("hearingOption"))) {
            caseDetails.getData().remove("hearingOption");
        } else {
            throw new IllegalStateException(format("Case %s does not have hearing option", caseDetails.getId()));
        }
    }

    private void run3239(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3239", "DE21C50042", caseData);

        if (isNotEmpty(caseData.getSubmittedForm())
            && isNotEmpty(caseData.getCorrespondenceDocuments().get(0))
            && caseData.getCorrespondenceDocuments().get(0).getValue().getName().equals("Redacted C110a")) {
            caseDetails.getData().put("submittedForm",
                caseData.getCorrespondenceDocuments().get(0).getValue().getDocument());
            caseData.getCorrespondenceDocuments().remove(0);
            caseDetails.getData().put("correspondenceDocuments", caseData.getCorrespondenceDocuments());
        } else {
            throw new IllegalStateException(format("Case %s does not have C110a/redacted copy", caseDetails.getId()));
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
