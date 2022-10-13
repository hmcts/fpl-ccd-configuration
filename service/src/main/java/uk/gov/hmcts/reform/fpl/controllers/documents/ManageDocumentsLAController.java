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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.document.PartyListGenerator;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ConfidentialBundleHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentSubtypeListLA.RESPONDENT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.DOCUMENT_SUB_TYPE;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RELATED_TO_HEARING;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CASE_SUMMARY_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CHILDREN_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.COURT_BUNDLE_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.COURT_BUNDLE_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_RESPONDENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.PLACEMENT_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_CHILD_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.POSITION_STATEMENT_RESPONDENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SKELETON_ARGUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.TEMP_EVIDENCE_DOCUMENTS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/manage-documents-la")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsLAController extends CallbackController {
    private static final boolean NOT_SOLICITOR = false;
    private final ManageDocumentLAService manageDocumentLAService;
    private final ManageDocumentService manageDocumentService;
    private final ApplicationDocumentsService applicationDocumentsService;
    private final ConfidentialDocumentsSplitter splitter;
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final DocumentListService documentListService;
    private final UserService userService;
    private final PartyListGenerator partyListGenerator;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(manageDocumentLAService.baseEventData(caseData));
        caseDetails.getData().put("hearingDocumentsPartyList", partyListGenerator.buildPartyList(caseData));

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
            case HEARING_DOCUMENTS:
                if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
                    return respond(caseDetails, List.of("There are no hearings to associate a hearing document with"));
                }
                caseDetails.getData().putAll(manageDocumentService.initialiseHearingDocumentFields(caseData));
                break;
            case PLACEMENT_NOTICE_RESPONSE:
                Map<String, Object> fields = manageDocumentService.initialisePlacementHearingResponseFields(
                    caseData, PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY);
                caseDetails.getData().putAll(fields);
                break;
        }

        caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_KEY, supportingEvidence);
        return respond(caseDetails);
    }

    @PostMapping("/further-evidence-documents/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleFurtherEvidenceMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (OTHER.equals(caseData.getManageDocumentSubtypeListLA())) {
            caseDetails.getData().putAll(manageDocumentService.initialiseHearingListAndLabel(caseData));

            List<Element<SupportingEvidenceBundle>> supportingEvidence
                = manageDocumentService.getFurtherEvidences(caseData, caseData.getFurtherEvidenceDocumentsLA());

            caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_KEY, supportingEvidence);
        } else if (RESPONDENT_STATEMENT.equals(caseData.getManageDocumentSubtypeListLA())) {
            UUID selectedRespondentId = manageDocumentService.getSelectedRespondentId(caseData);

            caseDetails.getData().put(RESPONDENTS_LIST_KEY,
                caseData.buildRespondentDynamicList(selectedRespondentId));

            caseDetails.getData().put(TEMP_EVIDENCE_DOCUMENTS_KEY,
                manageDocumentService.getRespondentStatements(caseData, selectedRespondentId));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        ManageDocumentLA manageDocumentLA = Optional.ofNullable(caseData.getManageDocumentLA())
            .orElseThrow(() -> new IllegalStateException("Unexpected null manage document LA. " + caseData));

        ManageDocumentTypeListLA manageDocumentLAType = manageDocumentLA.getType();
        switch (manageDocumentLAType) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> currentBundle;

                if (RESPONDENT_STATEMENT.equals(caseData.getManageDocumentSubtypeListLA())) {
                    List<Element<RespondentStatement>> respondentStatements =
                        manageDocumentService.getUpdatedRespondentStatements(caseData, NOT_SOLICITOR);
                    caseDetailsMap.putIfNotEmpty("respondentStatements", respondentStatements);
                    caseDetailsMap.put(DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY,
                        manageDocumentService.getDocumentsWithConfidentialAddress(caseData,
                            ConfidentialBundleHelper.getSupportingEvidenceBundle(
                                ElementUtils.unwrapElements(caseData.getRespondentStatements())),
                            ConfidentialBundleHelper.getSupportingEvidenceBundle(
                                ElementUtils.unwrapElements(respondentStatements))));
                } else if (APPLICATION_DOCUMENTS.equals(caseData.getManageDocumentSubtypeListLA())) {
                    //Application documents

                    caseDetailsMap.putIfNotEmpty(applicationDocumentsService.updateApplicationDocuments(
                        caseData.getApplicationDocuments(), caseDataBefore.getApplicationDocuments()
                    ));
                    //Hearing related evidence
                } else if (YES.getValue().equals(caseData.getManageDocumentsRelatedToHearing())) {
                    currentBundle = manageDocumentService.setDateTimeOnHearingFurtherEvidenceSupportingEvidence(
                        caseData, caseDataBefore, NOT_SOLICITOR
                    );

                    List<Element<HearingFurtherEvidenceBundle>> updatedBundle =
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle);

                    caseDetailsMap.putIfNotEmpty(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_KEY, updatedBundle
                    );
                    caseDetailsMap.put(DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY,
                        manageDocumentService.getDocumentsWithConfidentialAddress(caseData,
                            ConfidentialBundleHelper.getSupportingEvidenceBundle(
                                ElementUtils.unwrapElements(caseData.getHearingFurtherEvidenceDocuments())),
                            ConfidentialBundleHelper.getSupportingEvidenceBundle(
                                ElementUtils.unwrapElements(updatedBundle))));
                    //Non-hearing-related evidence
                } else {
                    currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getFurtherEvidenceDocumentsLA(),
                        NOT_SOLICITOR
                    );

                    splitter.updateConfidentialDocsInCaseDetails(
                        caseDetailsMap, currentBundle, FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY
                    );
                    caseDetailsMap.putIfNotEmpty(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY, currentBundle);
                    caseDetailsMap.put(DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY,
                        manageDocumentService.getDocumentsWithConfidentialAddress(caseData,
                            caseData.getFurtherEvidenceDocumentsLA(),
                            currentBundle));
                }
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getSupportingEvidenceDocumentsTemp(), caseDataBefore.getCorrespondenceDocumentsLA(),
                        NOT_SOLICITOR
                    );

                List<Element<SupportingEvidenceBundle>> sortedDocuments
                    = manageDocumentService.sortCorrespondenceDocumentsByUploadedDate(updatedCorrespondenceDocuments);

                splitter.updateConfidentialDocsInCaseDetails(
                    caseDetailsMap, sortedDocuments, CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY
                );
                caseDetailsMap.putIfNotEmpty(CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY, sortedDocuments);
                caseDetailsMap.put(DOCUMENT_WITH_CONFIDENTIAL_ADDRESS_KEY,
                    manageDocumentService.getDocumentsWithConfidentialAddress(caseData,
                        caseData.getCorrespondenceDocumentsLA(),
                        sortedDocuments));
                break;
            case ADDITIONAL_APPLICATIONS_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(
                    manageDocumentService.buildFinalApplicationBundleSupportingDocuments(caseData, NOT_SOLICITOR));
                break;
            case HEARING_DOCUMENTS:
                caseDetailsMap.putIfNotEmpty(manageDocumentService.buildHearingDocumentList(caseData));
                break;
            case PLACEMENT_NOTICE_RESPONSE:
                PlacementEventData eventData = manageDocumentService.updatePlacementNoticesLA(caseData);
                caseDetailsMap.putIfNotEmpty("placements", eventData.getPlacements());
                break;
        }

        removeTemporaryFields(caseDetailsMap, TEMP_EVIDENCE_DOCUMENTS_KEY, MANAGE_DOCUMENT_LA_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, SUPPORTING_C2_LABEL, MANAGE_DOCUMENTS_HEARING_LIST_KEY,
            SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENTS_HEARING_LABEL_KEY, HEARING_DOCUMENT_HEARING_LIST_KEY,
            HEARING_DOCUMENT_TYPE, COURT_BUNDLE_HEARING_LABEL_KEY, COURT_BUNDLE_KEY, CASE_SUMMARY_KEY,
            POSITION_STATEMENT_CHILD_KEY, POSITION_STATEMENT_RESPONDENT_KEY, DOCUMENT_SUB_TYPE, RELATED_TO_HEARING,
            RESPONDENTS_LIST_KEY, CHILDREN_LIST_KEY, HEARING_DOCUMENT_RESPONDENT_LIST_KEY, PLACEMENT_LIST_KEY,
            SKELETON_ARGUMENT_KEY, "hearingDocumentsPartyList", "placementNoticeResponses", "placement");

        CaseDetails details = CaseDetails.builder().data(caseDetailsMap).build();
        caseDetailsMap.putAll(documentListService.getDocumentView(getCaseData(details)));

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        final UserDetails userDetails = idamClient.getUserDetails(requestData.authorisation());
        final CaseData caseData = getCaseData(request);
        final CaseData caseDataBefore = getCaseDataBefore(request);

        publishEvent(new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, getUploaderType(caseData),
            userDetails));
    }

    private DocumentUploaderType getUploaderType(CaseData caseData) {
        final Set<CaseRole> caseRoles = userService.getCaseRoles(caseData.getId());

        if (caseRoles.contains(LASHARED)) {
            return DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
        }

        return DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
    }

}
