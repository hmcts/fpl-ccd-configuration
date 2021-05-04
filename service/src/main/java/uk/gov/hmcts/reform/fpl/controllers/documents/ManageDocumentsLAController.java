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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.RESPONDENT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.DOCUMENT_SUB_TYPE;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RELATED_TO_HEARING;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENT_STATEMENT_LIST_KEY;
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
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final FeatureToggleService featureToggleService;
    private final DocumentListService documentListService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(manageDocumentLAService.baseEventData(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<SupportingEvidenceBundle>> supportingEvidence = new ArrayList<>();

        caseDetails.getData().putAll(manageDocumentLAService.baseEventData(caseData));

        switch (caseData.getManageDocumentLA().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                caseDetails.getData().put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());
                return respond(caseDetails);
            case CORRESPONDENCE:
                supportingEvidence = manageDocumentService.getSupportingEvidenceBundle(
                    caseData.getCorrespondenceDocumentsLA());
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                if (!caseData.hasApplicationBundles()) {
                    return respond(caseDetails, List.of(
                        "There are no additional applications to associate supporting documents with"));
                }
                caseDetails.getData().putAll(manageDocumentService.initialiseApplicationBundlesListAndLabel(caseData));
                supportingEvidence = manageDocumentService.getApplicationsSupportingEvidenceBundles(caseData);
                break;
            case COURT_BUNDLE:
                if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
                    return respond(caseDetails, List.of("There are no hearings to associate a bundle with"));
                }
                caseDetails.getData().putAll(manageDocumentLAService.initialiseCourtBundleFields(caseData));
                break;
        }

        caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, supportingEvidence);
        return respond(caseDetails);
    }

    @PostMapping("/further-evidence-documents/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleFurtherEvidenceMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (OTHER.equals(caseData.getManageDocumentSubtypeListLA())) {
            caseDetails.getData().putAll(manageDocumentService.initialiseHearingListAndLabel(
                caseData, YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())));

            List<Element<SupportingEvidenceBundle>> supportingEvidence
                = manageDocumentService.getFurtherEvidenceCollection(caseData,
                YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing()),
                caseData.getFurtherEvidenceDocumentsLA());

            caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, supportingEvidence);
        } else if (RESPONDENT_STATEMENT.equals(caseData.getManageDocumentSubtypeListLA())) {
            UUID selectedRespondentId = manageDocumentService.getSelectedRespondentId(caseData);

            caseDetails.getData().put(RESPONDENT_STATEMENT_LIST_KEY,
                caseData.buildRespondentStatementDynamicList(selectedRespondentId));

            caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                manageDocumentService.getRespondentStatementFurtherEvidenceCollection(caseData, selectedRespondentId));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        switch (caseData.getManageDocumentLA().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> currentBundle;

                if (RESPONDENT_STATEMENT.equals(caseData.getManageDocumentSubtypeListLA())) {
                    caseDetailsMap.putIfNotEmpty("respondentStatements",
                        manageDocumentService.getUpdatedRespondentStatements(caseData));
                } else if (APPLICATION_DOCUMENTS.equals(caseData.getManageDocumentSubtypeListLA())) {
                    //Application documents

                    caseDetailsMap.putIfNotEmpty(applicationDocumentsService.updateApplicationDocuments(
                        caseData.getApplicationDocuments(), caseDataBefore.getApplicationDocuments()
                    ));
                    //Hearing related evidence
                } else if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
                    currentBundle = manageDocumentService.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
                        caseData, caseDataBefore
                    );

                    List<Element<HearingFurtherEvidenceBundle>> updatedBundle =
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    caseDetailsMap.putIfNotEmpty(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, updatedBundle
                    );
                    //Non-hearing-related evidence
                } else {
                    currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getFurtherEvidenceDocumentsLA()
                    );

                    splitter.updateConfidentialDocsInCaseDetails(
                        caseDetailsMap, currentBundle, FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY
                    );
                    caseDetailsMap.putIfNotEmpty(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY, currentBundle);
                }
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getCorrespondenceDocumentsLA()
                    );

                splitter.updateConfidentialDocsInCaseDetails(
                    caseDetailsMap, updatedCorrespondenceDocuments, CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY
                );
                caseDetailsMap.putIfNotEmpty(CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY, updatedCorrespondenceDocuments);
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(
                    manageDocumentService.buildFinalApplicationBundleSupportingDocuments(caseData));
                break;
            case COURT_BUNDLE:
                caseDetailsMap.putIfNotEmpty(COURT_BUNDLE_LIST_KEY, manageDocumentLAService
                    .buildCourtBundleList(caseData));
                break;
        }

        removeTemporaryFields(caseDetailsMap, TEMP_EVIDENCE_DOCUMENTS_COLLECTION_KEY, MANAGE_DOCUMENT_LA_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY, COURT_BUNDLE_HEARING_LIST_KEY,
            COURT_BUNDLE_KEY, DOCUMENT_SUB_TYPE, RELATED_TO_HEARING, RESPONDENT_STATEMENT_LIST_KEY);

        if (featureToggleService.isFurtherEvidenceDocumentTabEnabled()) {
            CaseDetails details = CaseDetails.builder().data(caseDetailsMap).build();
            caseDetailsMap.put("documentsList", documentListService.getDocumentsList(getCaseData(details), false));
        }

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        if (this.featureToggleService.isFurtherEvidenceUploadNotificationEnabled()) {
            UserDetails userDetails = idamClient.getUserDetails(requestData.authorisation());

            publishEvent(new FurtherEvidenceUploadedEvent(getCaseData(request), getCaseDataBefore(request),
                true, userDetails));
        }
    }
}
