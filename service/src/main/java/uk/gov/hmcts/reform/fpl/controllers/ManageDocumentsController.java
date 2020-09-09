package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.SupportingEvidenceValidatorService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;

@Api
@RestController
@RequestMapping("/callback/manage-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsController {
    private final ObjectMapper mapper;
    private final ManageDocumentService manageDocumentService;
    private final SupportingEvidenceValidatorService supportingEvidenceValidatorService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put(MANAGE_DOCUMENTS_HEARING_LIST_KEY, caseData.buildDynamicHearingList());

        if (hasC2DocumentBundle(caseData)) {
            caseDetails.getData().put(SUPPORTING_C2_LIST_KEY, caseData.buildC2DocumentDynamicList());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/initialise-manage-document-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        switch (caseData.getManageDocument().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                caseDetails.getData().putAll(manageDocumentService.initialiseHearingListAndLabel(caseData));

                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments =
                    manageDocumentService.getFurtherEvidenceCollection(caseDetails);

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceDocuments);
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> correspondenceDocuments =
                    manageDocumentService.getSupportingEvidenceBundle(caseData.getCorrespondenceDocuments());

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, correspondenceDocuments);
                break;
            case C2:
                // TODO
                // Populate data for case type is C2
                break;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/validate-further-evidence/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateFurtherEvidenceDocuments(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(supportingEvidenceValidatorService.validate(caseData.getFurtherEvidenceDocumentsTEMP()))
            .build();
    }

    @PostMapping("/validate-correspondence-documents/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateCorrespondingDocuments(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(supportingEvidenceValidatorService.validate(caseData.getCorrespondenceDocuments()))
            .build();
    }

    @PostMapping("/validate-c2-supporting-documents/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateC2SupportingDocuments(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(supportingEvidenceValidatorService.validate(caseData.getC2SupportingDocuments()))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = mapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        switch (caseData.getManageDocument().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> previousFurtherEvidenceDocuments
                    = getPreviousSupportingEvidenceBundle(caseDataBefore.getFurtherEvidenceDocumentsTEMP());

                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getFurtherEvidenceDocumentsTEMP(), previousFurtherEvidenceDocuments);

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceDocuments);
                manageDocumentService.buildFinalFurtherEvidenceCollection(caseDetails);
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> previousCorrespondenceDocuments
                    = getPreviousSupportingEvidenceBundle(caseDataBefore.getCorrespondenceDocuments());

                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                        caseData.getCorrespondenceDocuments(), previousCorrespondenceDocuments);

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, updatedCorrespondenceDocuments);
                break;
            case C2:
                // TODO
                // Populate data for case type is C2
                break;
        }

        caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, null);
        caseDetails.getData().remove(MANAGE_DOCUMENT_KEY);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Element<SupportingEvidenceBundle>> getPreviousSupportingEvidenceBundle(
        List<Element<SupportingEvidenceBundle>> previousSupportingEvidenceBundle) {
        return previousSupportingEvidenceBundle != null ? previousSupportingEvidenceBundle : List.of();
    }

    private boolean hasC2DocumentBundle(CaseData caseData) {
        return caseData.getC2DocumentBundle() != null && !caseData.getC2DocumentBundle().isEmpty();
    }
}
