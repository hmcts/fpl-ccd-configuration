package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.SupportingEvidenceValidatorService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.COURT_BUNDLE_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/manage-documents-la")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsLAController extends CallbackController {
    private final ManageDocumentLAService manageDocumentLAService;
    private final ManageDocumentService manageDocumentService;
    private final SupportingEvidenceValidatorService supportingEvidenceValidatorService;
    private final ApplicationDocumentsService applicationDocumentsService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(
            manageDocumentService.initialiseManageDocumentEvent(caseData, MANAGE_DOCUMENT_LA_KEY));

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

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        switch (caseData.getManageDocumentLA().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                caseDetails.getData().putAll(manageDocumentService.initialiseHearingListAndLabel(
                    caseData, caseData.getManageDocumentLA().isDocumentRelatedToHearing()));
                supportingEvidence = manageDocumentService.getFurtherEvidenceCollection(
                    caseData,
                    caseData.getManageDocumentLA().isDocumentRelatedToHearing(),
                    caseData.getFurtherEvidenceDocumentsLA()
                );
                break;
            case CORRESPONDENCE:
                supportingEvidence = manageDocumentService.getSupportingEvidenceBundle(
                    caseData.getCorrespondenceDocumentsLA());
                break;
            case C2:
                if (!caseData.hasC2DocumentBundle()) {
                    return respond(caseDetails, List.of("There are no C2s to associate supporting documents with"));
                }
                caseDetails.getData().putAll(manageDocumentService.initialiseC2DocumentListAndLabel(caseData));
                supportingEvidence = manageDocumentService.getC2SupportingEvidenceBundle(caseData);
                break;
            case COURT_BUNDLE:
                if (caseData.getCourtBundleList().isEmpty()) {
                    return respond(caseDetails, List.of("There are no hearings to associate a bundle with"));
                }
                caseDetails.getData().putAll(manageDocumentLAService.initialiseCourtBundleFields(caseData));
                break;
            case APPLICATION:
                break;
        }

        caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, supportingEvidence);
        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);

        ManageDocumentLA manageDocumentLA = caseData.getManageDocumentLA();
        switch (manageDocumentLA.getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> currentBundle;

                if (manageDocumentLA.isDocumentRelatedToHearing()) {
                    currentBundle = manageDocumentService.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
                        caseData, caseDataBefore);

                    caseDetails.getData().put(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle)
                    );
                } else {
                    currentBundle = manageDocumentService
                        .setDateTimeUploadedOnSupportingEvidence(caseData.getSupportingEvidenceDocumentsTemp(),
                            caseDataBefore.getFurtherEvidenceDocumentsLA());
                    caseDetails.getData().put(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY, currentBundle);
                }
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments = manageDocumentService
                    .setDateTimeUploadedOnSupportingEvidence(caseData.getSupportingEvidenceDocumentsTemp(),
                        caseDataBefore.getCorrespondenceDocumentsLA()
                    );

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY, updatedCorrespondenceDocuments);
                break;
            case C2:
                List<Element<C2DocumentBundle>> updatedC2Documents = manageDocumentService
                    .buildFinalC2SupportingDocuments(caseData);

                caseDetails.getData().put(C2_DOCUMENTS_COLLECTION_KEY, updatedC2Documents);
                break;
            case COURT_BUNDLE:
                caseDetails.getData().put(COURT_BUNDLE_LIST_KEY,
                    manageDocumentLAService.buildCourtBundleList(caseData));
                break;
            case APPLICATION:
                caseDetails.getData().putAll(applicationDocumentsService.updateApplicationDocuments(
                    caseData.getApplicationDocuments(), caseDataBefore.getApplicationDocuments()));
                break;
        }

        removeTemporaryFields(caseDetails, TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, MANAGE_DOCUMENT_LA_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY, COURT_BUNDLE_HEARING_LIST_KEY,
            COURT_BUNDLE_KEY);

        return respond(caseDetails);
    }
}
