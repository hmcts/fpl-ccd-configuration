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
import uk.gov.hmcts.reform.fpl.service.ApplicationDocumentsService;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Api
@RestController
@RequestMapping("/callback/upload-application-documents")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadApplicationDocumentsController extends CallbackController {
    private final ApplicationDocumentsService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        caseDetails.getData().remove("showMetaFields");

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseData caseDataBefore = getCaseDataBefore(callbackrequest);
        caseDetails.getData().putAll(service.updateCaseDocuments(caseData.getDocuments(), caseDataBefore.getDocuments()));

        caseDetails.getData().put("showMetaFields", YES);

        return respond(caseDetails);
    }
}
