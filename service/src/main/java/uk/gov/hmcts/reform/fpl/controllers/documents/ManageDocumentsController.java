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
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SupportingEvidenceValidatorService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeList.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeList.RESPONDENT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.ADDITIONAL_APPLICATIONS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_SOLICITOR_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_SOLICITOR_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/manage-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsController extends CallbackController {

    private final UserService userService;
    private final FeatureToggleService featureToggleService;
    private final ManageDocumentService documentService;
    private final SupportingEvidenceValidatorService supportingEvidenceValidatorService;
    private final ConfidentialDocumentsSplitter confidentialDocuments;
    private final DocumentListService documentListService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(documentService.baseEventData(caseData));

        caseDetails.getData().remove("furtherEvidenceDocumentsTEMP");

        return respond(caseDetails);
    }

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentType type = caseData.getManageDocument().getType();
        boolean isSolicitor = isSolicitor(caseData.getId());

        if (isSolicitor && userService.isHmctsUser()) {
            throw new IllegalStateException(
                String.format("User %s is HMCTS but has solicitor case roles", userService.getUserEmail()));
        }

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        caseDetails.getData().putAll(documentService.baseEventData(caseData));

        if (CORRESPONDENCE == type) {
            supportingEvidence = documentService.getSupportingEvidenceBundle(
                isSolicitor ? caseData.getCorrespondenceDocumentsSolicitor() : caseData.getCorrespondenceDocuments());
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
        boolean isSolicitor = isSolicitor(caseData.getId());

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        if (caseData.getManageDocumentSubtypeList() == OTHER) {
            caseDetailsMap.putAll(documentService.initialiseHearingListAndLabel(caseData));
            supportingEvidence = documentService.getFurtherEvidences(caseData,
                isSolicitor ? caseData.getFurtherEvidenceDocumentsSolicitor() : caseData.getFurtherEvidenceDocuments());
        }

        //Respondent statements are unfiltered (everyone has same access) - is it correct?
        if (caseData.getManageDocumentSubtypeList() == RESPONDENT_STATEMENT) {
            UUID respondentId = documentService.getSelectedRespondentId(caseData);
            supportingEvidence = documentService.getRespondentStatements(caseData, respondentId);
            caseDetailsMap.put(RESPONDENTS_LIST_KEY, caseData.buildRespondentDynamicList(respondentId));
        }

        caseDetailsMap.put(TEMP_EVIDENCE_DOCUMENTS_KEY, supportingEvidence);

        return respond(caseDetailsMap);
    }

    @PostMapping("/validate-supporting-evidence/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateSupportingEvidence(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<SupportingEvidenceBundle>> supportingEvidence = caseData.getSupportingEvidenceDocumentsTemp();
        List<String> errors = supportingEvidenceValidatorService.validate(supportingEvidence);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        boolean isSolicitor = isSolicitor(caseData.getId());

        ManageDocumentType manageDocumentType = caseData.getManageDocument().getType();
        List<Element<SupportingEvidenceBundle>> currentBundle;
        switch (manageDocumentType) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                if (caseData.getManageDocumentSubtypeList() == RESPONDENT_STATEMENT) {

                    caseDetailsMap.putIfNotEmpty("respondentStatements",
                        documentService.getUpdatedRespondentStatements(caseData, isSolicitor));

                } else if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
                    currentBundle = documentService
                        .setDateTimeOnHearingFurtherEvidenceSupportingEvidence(caseData, caseDataBefore, isSolicitor);

                    var updatedBundle = documentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    caseDetailsMap.putIfNotEmpty(HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY, updatedBundle);
                } else {
                    currentBundle = documentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(),
                        isSolicitor ? caseDataBefore.getFurtherEvidenceDocumentsSolicitor() :
                            caseDataBefore.getFurtherEvidenceDocuments(), isSolicitor);

                    if (!isSolicitor) {
                        confidentialDocuments.updateConfidentialDocsInCaseDetails(caseDetailsMap, currentBundle,
                            FURTHER_EVIDENCE_DOCUMENTS_KEY);
                        caseDetailsMap.putIfNotEmpty(FURTHER_EVIDENCE_DOCUMENTS_KEY, currentBundle);
                    } else {
                        caseDetailsMap.putIfNotEmpty(FURTHER_EVIDENCE_DOCUMENTS_SOLICITOR_KEY, currentBundle);
                    }
                }
                break;
            case CORRESPONDENCE:
                currentBundle = documentService.setDateTimeUploadedOnSupportingEvidence(
                    caseData.getSupportingEvidenceDocumentsTemp(),
                    isSolicitor ? caseDataBefore.getCorrespondenceDocumentsSolicitor() :
                        caseDataBefore.getCorrespondenceDocuments(), isSolicitor);

                List<Element<SupportingEvidenceBundle>> sortedBundle
                    = documentService.sortCorrespondenceDocumentsByUploadedDate(currentBundle);

                if (!isSolicitor) {
                    confidentialDocuments.updateConfidentialDocsInCaseDetails(caseDetailsMap, sortedBundle,
                        CORRESPONDING_DOCUMENTS_COLLECTION_KEY);
                    caseDetailsMap.putIfNotEmpty(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, sortedBundle);
                } else {
                    caseDetailsMap.putIfNotEmpty(CORRESPONDING_DOCUMENTS_COLLECTION_SOLICITOR_KEY, sortedBundle);
                }
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(
                    documentService.buildFinalApplicationBundleSupportingDocuments(caseData, isSolicitor));
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

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        publishEvent(new FurtherEvidenceUploadedEvent(getCaseData(request), getCaseDataBefore(request), false));
    }

    private boolean isSolicitor(Long caseId) {
        return userService.hasAnyCaseRoleFrom(representativeSolicitors(), caseId);
    }
}
