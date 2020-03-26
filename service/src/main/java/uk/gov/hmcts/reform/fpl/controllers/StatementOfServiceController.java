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
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.service.StatementOfServiceService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/statement-of-service")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatementOfServiceController {
    private static final String CONSENT_TEMPLATE = "I, %s, have served the documents as stated.";
    private final UserDetailsService userDetailsService;
    private final MapperService mapperService;
    private final StatementOfServiceService statementOfServiceService;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("statementOfService", statementOfServiceService.expandRecipientCollection(caseData));

        String label = String.format(CONSENT_TEMPLATE, userDetailsService.getUserName());

        Map<String, Object> data = caseDetails.getData();
        data.put("serviceDeclarationLabel", label);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}

