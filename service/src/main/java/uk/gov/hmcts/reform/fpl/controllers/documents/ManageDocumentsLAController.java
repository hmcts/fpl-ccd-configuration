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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/manage-documents-la")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsLAController extends CallbackController {
    private final ManageDocumentLAService manageDocumentLAService;
    private final ManageDocumentService manageDocumentService;
    private final ApplicationDocumentsService applicationDocumentsService;
    private final ConfidentialDocumentsSplitter splitter;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(
            manageDocumentService.initialiseManageDocumentEvent(caseData, MANAGE_DOCUMENT_LA_KEY));

        return respond(caseDetails);
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
                if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
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

        Map<String, Object> confidentialAndNonConfidentialDocuments = new HashMap<>();
        ManageDocumentLA manageDocumentLA = caseData.getManageDocumentLA();
        switch (manageDocumentLA.getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> currentBundle;

                if (manageDocumentLA.isDocumentRelatedToHearing()) {
                    currentBundle = manageDocumentService.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
                        caseData, caseDataBefore
                    );

                    List<Element<HearingFurtherEvidenceBundle>> updatedBundle =
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    confidentialAndNonConfidentialDocuments.put(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, updatedBundle
                    );

                } else {
                    currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getFurtherEvidenceDocumentsLA()
                    );

                    confidentialAndNonConfidentialDocuments = splitter.splitIntoAllAndNonConfidential(
                        currentBundle, FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY
                    );
                }
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getCorrespondenceDocumentsLA()
                    );

                confidentialAndNonConfidentialDocuments = splitter.splitIntoAllAndNonConfidential(
                    updatedCorrespondenceDocuments, CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY
                );
                break;
            case C2:
                List<Element<C2DocumentBundle>> updatedC2Documents =
                    manageDocumentService.buildFinalC2SupportingDocuments(caseData);

                confidentialAndNonConfidentialDocuments.put(C2_DOCUMENTS_COLLECTION_KEY, updatedC2Documents);
                break;
            case COURT_BUNDLE:
                confidentialAndNonConfidentialDocuments.put(
                    COURT_BUNDLE_LIST_KEY, manageDocumentLAService.buildCourtBundleList(caseData)
                );
                break;
            case APPLICATION:
                confidentialAndNonConfidentialDocuments = applicationDocumentsService.updateApplicationDocuments(
                    caseData.getApplicationDocuments(), caseDataBefore.getApplicationDocuments()
                );
                break;
        }

        caseDetails.getData().putAll(confidentialAndNonConfidentialDocuments);

        removeTemporaryFields(caseDetails, TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, MANAGE_DOCUMENT_LA_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY, COURT_BUNDLE_HEARING_LIST_KEY,
            COURT_BUNDLE_KEY);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        if(this.featureToggleService.isFurtherEvidenceEventEnabled()) {
            CaseDetails caseDetails = request.getCaseDetails();
            CaseData caseData = getCaseData(caseDetails);

            ManageDocumentLA manageDocumentLA = caseData.getManageDocumentLA();


            switch (manageDocumentLA.getType()) {
                case FURTHER_EVIDENCE_DOCUMENTS:
                    break;
            }
        }
    }
}
