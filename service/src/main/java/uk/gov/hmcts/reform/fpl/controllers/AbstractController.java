package uk.gov.hmcts.reform.fpl.controllers;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

public class AbstractController {

    public AboutToStartOrSubmitCallbackResponse save(CaseDetails caseDetails){
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    public AboutToStartOrSubmitCallbackResponse save(CaseDetails caseDetails, List<String> errors){
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(errors)
            .build();
    }



}
