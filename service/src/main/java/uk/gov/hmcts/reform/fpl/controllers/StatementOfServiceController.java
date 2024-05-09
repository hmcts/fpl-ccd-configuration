package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.StatementOfServiceService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@RestController
@RequestMapping("/callback/statement-of-service")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatementOfServiceController extends CallbackController {
    private static final String CONSENT_TEMPLATE = "I, %s, have served the documents as stated.";
    private final IdamClient idamClient;
    private final StatementOfServiceService statementOfServiceService;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        String label = String.format(CONSENT_TEMPLATE, idamClient.getUserInfo(requestData.authorisation()).getName());

        caseDetails.getData().put("statementOfService", statementOfServiceService.expandRecipientCollection(caseData));
        caseDetails.getData().put("serviceDeclarationLabel", label);

        return respond(caseDetails);
    }
}

