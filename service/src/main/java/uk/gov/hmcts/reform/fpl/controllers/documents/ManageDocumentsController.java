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
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SupportingEvidenceValidatorService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/manage-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsController extends CallbackController {
    private final ManageDocumentService manageDocumentService;
    private final SupportingEvidenceValidatorService supportingEvidenceValidatorService;
    private final ConfidentialDocumentsSplitter splitter;
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final FeatureToggleService featureToggleService;
    private final DocumentListService documentListService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(manageDocumentService.baseEventData(caseData));

        caseDetails.getData().remove("furtherEvidenceDocumentsTEMP");

        return respond(caseDetails);
    }

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        caseDetails.getData().putAll(manageDocumentService.baseEventData(caseData));

        switch (caseData.getManageDocument().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                caseDetails.getData().putAll(
                    manageDocumentService.initialiseHearingListAndLabel(
                        caseData, caseData.getManageDocument().isDocumentRelatedToHearing()
                    )
                );

                supportingEvidence = manageDocumentService.getFurtherEvidenceCollection(
                    caseData,
                    caseData.getManageDocument().isDocumentRelatedToHearing(),
                    caseData.getFurtherEvidenceDocuments()
                );
                break;
            case CORRESPONDENCE:
                supportingEvidence = manageDocumentService.getSupportingEvidenceBundle(
                    caseData.getCorrespondenceDocuments()
                );
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                if (!caseData.hasApplicationBundles()) {
                    return respond(caseDetails,
                        List.of("There are no additional applications to associate supporting documents with"));
                }
                caseDetails.getData().putAll(manageDocumentService.initialiseApplicationBundlesListAndLabel(caseData));
                supportingEvidence = manageDocumentService.getApplicationsSupportingEvidenceBundles(caseData);
                break;
        }

        caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, supportingEvidence);
        return respond(caseDetails);
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
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        ManageDocument manageDocument = caseData.getManageDocument();
        List<Element<SupportingEvidenceBundle>> currentBundle;
        switch (manageDocument.getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                if (manageDocument.isDocumentRelatedToHearing()) {
                    currentBundle = manageDocumentService.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
                        caseData, caseDataBefore
                    );

                    List<Element<HearingFurtherEvidenceBundle>> updatedBundle =
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    caseDetailsMap.putIfNotEmpty(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, updatedBundle
                    );
                } else {
                    currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getFurtherEvidenceDocuments()
                    );

                    splitter.updateConfidentialDocsInCaseDetails(
                        caseDetailsMap, currentBundle, FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY
                    );
                    caseDetailsMap.putIfNotEmpty(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, currentBundle);
                }
                break;
            case CORRESPONDENCE:
                currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                    caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getCorrespondenceDocuments()
                );

                splitter.updateConfidentialDocsInCaseDetails(
                    caseDetailsMap, currentBundle, CORRESPONDING_DOCUMENTS_COLLECTION_KEY
                );
                caseDetailsMap.putIfNotEmpty(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, currentBundle);
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(
                    manageDocumentService.buildFinalApplicationBundleSupportingDocuments(caseData));
                break;
        }

        removeTemporaryFields(caseDetailsMap, TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, MANAGE_DOCUMENT_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY);

        if (featureToggleService.isFurtherEvidenceDocumentTabEnabled()) {
            CaseDetails details = CaseDetails.builder().data(caseDetailsMap).build();
            caseDetailsMap.put("documentsList", documentListService.getDocumentsList(getCaseData(details)));
        }

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        if (this.featureToggleService.isFurtherEvidenceUploadNotificationEnabled()) {
            UserDetails userDetails = idamClient.getUserDetails(requestData.authorisation());

            publishEvent(new FurtherEvidenceUploadedEvent(getCaseData(request),
                getCaseDataBefore(request), false,
                userDetails));
        }
    }
}
