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

        // TODO
        // Populate C2 supporting list

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/initialise-manage-documents-collections/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        // TODO
        // Validate date and time inputted

        switch (caseData.getManageDocument().getType()) {
            case FURTHER_EVIDENCE_DOCUMENTS:
                manageDocumentService.initialiseFurtherEvidenceFields(caseDetails);

                List<Element<SupportingEvidenceBundle>> furtherEvidenceDocuments =
                    manageDocumentService.initialiseFurtherDocumentBundleCollection(caseDetails);

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, furtherEvidenceDocuments);
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> correspondenceDocuments =
                    manageDocumentService.initialiseSupportingEvidenceBundleCollection(
                        caseData.getCorrespondenceDocuments());

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

    // TODO
    // Refactor the below to just make use of the one mid event (hopefully possible)
    // Currently have multiple to support differing collection heading and hint text

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

    @PostMapping("/validate-corresponding-documents/mid-event")
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
                List<Element<SupportingEvidenceBundle>> updatedFurtherEvidenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnManageDocumentCollection(
                        caseData.getFurtherEvidenceDocumentsTEMP(), caseDataBefore.getFurtherEvidenceDocumentsTEMP());

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY,
                    updatedFurtherEvidenceDocuments);

                manageDocumentService.buildFurtherEvidenceCollection(caseDetails);

                caseDetails.getData().put(TEMP_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, null);
                break;
            case CORRESPONDENCE:
                List<Element<SupportingEvidenceBundle>> updatedCorrespondenceDocuments =
                    manageDocumentService.setDateTimeUploadedOnManageDocumentCollection(
                    caseData.getCorrespondenceDocuments(), caseDataBefore.getCorrespondenceDocuments());

                caseDetails.getData().put(CORRESPONDING_DOCUMENTS_COLLECTION_KEY, updatedCorrespondenceDocuments);
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
}
