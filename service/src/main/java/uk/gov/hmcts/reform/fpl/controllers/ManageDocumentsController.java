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
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.SupportingEvidenceValidatorService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.C2_SUPPORTING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LABEL;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

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

        caseDetails.getData().putAll(manageDocumentService.initialiseManageDocumentEvent(caseData));

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

                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments;
                furtherEvidenceDocuments = manageDocumentService.getFurtherEvidenceCollection(caseData);

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceDocuments);
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> correspondenceDocuments;
                correspondenceDocuments = manageDocumentService.getSupportingEvidenceBundle(
                    caseData.getCorrespondenceDocuments()
                );

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, correspondenceDocuments);
                break;
            case C2:
                caseDetails.getData().putAll(manageDocumentService.initialiseC2DocumentListAndLabel(caseData));

                caseDetails.getData().put(C2_SUPPORTING_DOCUMENTS_COLLECTION,
                    manageDocumentService.getC2SupportingEvidenceBundle(caseDetails));
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

        ManageDocument manageDocument = caseData.getManageDocument();
        switch (manageDocument.getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                List<Element<SupportingEvidenceBundle>> currentBundle;

                currentBundle = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                    caseData.getFurtherEvidenceDocumentsTEMP(), caseDataBefore.getFurtherEvidenceDocumentsTEMP()
                );

                if (manageDocument.isDocumentRelatedToHearing()) {
                    caseDetails.getData().put(
                        HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                        manageDocumentService.buildHearingFurtherEvidenceCollection(caseData, currentBundle)
                    );
                } else {
                    caseDetails.getData().put(FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, currentBundle);
                }
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments;

                updatedCorrespondenceDocuments = manageDocumentService.setDateTimeUploadedOnSupportingEvidence(
                    caseData.getCorrespondenceDocuments(), caseDataBefore.getCorrespondenceDocuments()
                );

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, updatedCorrespondenceDocuments);
                break;
            case C2:
                List<Element<C2DocumentBundle>> updatedC2Documents;

                updatedC2Documents = manageDocumentService.buildFinalC2SupportingDocuments(caseData);

                caseDetails.getData().put(C2_DOCUMENTS_COLLECTION_KEY, updatedC2Documents);
                break;
        }

        removeTemporaryFields(caseDetails, TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, MANAGE_DOCUMENT_KEY,
            C2_SUPPORTING_DOCUMENTS_COLLECTION, MANAGE_DOCUMENTS_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY,
            MANAGE_DOCUMENTS_HEARING_LABEL_KEY, SUPPORTING_C2_LABEL);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
