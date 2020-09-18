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
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentsValidatorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentsService;

import java.util.List;

@Api
@RestController
@RequestMapping("/callback/upload-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentsController extends CallbackController {
    private final DocumentsValidatorService documentsValidatorService;
    private final UploadDocumentsService uploadDocumentsService;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = documentsValidatorService.validateDocuments(caseData);

        if (errors.isEmpty()) {
            List<Element<DocumentSocialWorkOther>> listOfOtherDocs =
                uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                    caseDataBefore.getOtherSocialWorkDocuments(), caseData.getOtherSocialWorkDocuments());

            Document socialWorkChronologyDocument =
                uploadDocumentsService.setUpdatedByAndDateAndTimeForDocuments(
                    caseDataBefore.getSocialWorkChronologyDocument(),
                    caseData.getSocialWorkChronologyDocument());

            caseDetails.getData().put("documents_socialWorkOther", listOfOtherDocs);
            caseDetails.getData().put("socialWorkChronologyDocument", socialWorkChronologyDocument);
        }

        return respond(caseDetails, documentsValidatorService.validateDocuments(caseData));
    }
}
