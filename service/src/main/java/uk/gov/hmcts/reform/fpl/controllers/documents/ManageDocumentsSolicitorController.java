package uk.gov.hmcts.reform.fpl.controllers.documents;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeList.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeList.RESPONDENT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.ADDITIONAL_APPLICATIONS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

/*  The only differences in all of the callbacks is passing/using 'solicitor' case fields instead of 'admin' fields
 *  This could be improved to reduce code duplication, effectively only 4-5 lines are actually different
 */
@Api
@RestController
@RequestMapping("/callback/manage-documents-solicitor")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsSolicitorController extends CallbackController {

    private final FeatureToggleService featureToggleService;
    private final ManageDocumentService documentService;
    private final DocumentListService documentListService;

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentType type = caseData.getManageDocument().getType();

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        caseDetails.getData().putAll(documentService.baseEventData(caseData));

        if (CORRESPONDENCE == type) {
            supportingEvidence = documentService.getSupportingEvidenceBundle(
                caseData.getCorrespondenceDocumentsSolicitor());
        } else if (ADDITIONAL_APPLICATIONS_DOCUMENTS == type) {
            if (!caseData.hasApplicationBundles()) {
                return respond(caseDetails,
                    List.of("There are no additional applications to associate supporting documents with"));
            }
            caseDetails.getData().putAll(documentService.initialiseApplicationBundlesListAndLabel(caseData));
            supportingEvidence = documentService.getApplicationsSupportingEvidenceBundles(caseData);
        }

        caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_KEY, supportingEvidence);
        return respond(caseDetails);
    }

    @PostMapping("/further-evidence-documents/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleFurtherEvidenceMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        if (caseData.getManageDocumentSubtypeList() == OTHER) {
            caseDetailsMap.putAll(documentService.initialiseHearingListAndLabel(caseData));
            supportingEvidence = documentService.getFurtherEvidences(caseData,
                caseData.getFurtherEvidenceDocumentsSolicitor());
        }

        if (caseData.getManageDocumentSubtypeList() == RESPONDENT_STATEMENT) {
            UUID respondentId = documentService.getSelectedRespondentId(caseData);
            supportingEvidence = documentService.getRespondentStatements(caseData, respondentId);
            caseDetailsMap.put(RESPONDENTS_LIST_KEY, caseData.buildRespondentDynamicList(respondentId));
        }

        caseDetailsMap.put(TEMP_EVIDENCE_DOCUMENTS_KEY, supportingEvidence);

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        ManageDocumentType manageDocumentType = caseData.getManageDocument().getType();
        List<Element<SupportingEvidenceBundle>> currentBundle;
        switch (manageDocumentType) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                if (caseData.getManageDocumentSubtypeList() == RESPONDENT_STATEMENT) {

                    caseDetailsMap.putIfNotEmpty("respondentStatements",
                        documentService.getUpdatedRespondentStatements(caseData));

                } else if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
                    currentBundle = documentService
                        .setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore, true);

                    var updatedBundle = documentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    caseDetailsMap.putIfNotEmpty(HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY, updatedBundle);
                } else {
                    currentBundle = documentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getFurtherEvidenceDocuments(),
                        true);

                    caseDetailsMap.putIfNotEmpty("furtherEvidenceDocumentsSolicitor", currentBundle);
                }
                break;
            case CORRESPONDENCE:
                currentBundle = documentService.setDateTimeUploadedOnSupportingEvidence(
                    caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getCorrespondenceDocumentsSolicitor(),
                    true);

                List<Element<SupportingEvidenceBundle>> sortedBundle
                    = documentService.sortCorrespondenceDocumentsByUploadedDate(currentBundle);

                caseDetailsMap.putIfNotEmpty("correspondenceDocumentsSolicitor", sortedBundle);
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(
                    documentService.buildFinalApplicationBundleSupportingDocuments(caseData, true));
                break;
        }

        removeTemporaryFields(caseDetailsMap, TEMP_EVIDENCE_DOCUMENTS_KEY, MANAGE_DOCUMENT_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY, "manageDocumentSubtypeList",
            "manageDocumentsRelatedToHearing", "furtherEvidenceDocumentsTEMP");

        if (featureToggleService.isFurtherEvidenceDocumentTabEnabled()) {
            CaseDetails details = CaseDetails.builder().data(caseDetailsMap).build();
            caseDetailsMap.putAll(documentListService.getDocumentView(getCaseData(details)));
        }

        return respond(caseDetailsMap);
    }
}
