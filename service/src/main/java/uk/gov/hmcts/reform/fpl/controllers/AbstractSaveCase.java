package uk.gov.hmcts.reform.fpl.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.utils.CaseConverter;

public abstract class AbstractSaveCase {

    @Autowired
    private CaseConverter caseConverter;

    public AboutToStartOrSubmitCallbackResponse saveCase(CaseData caseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseConverter.convertToMap(caseData))
            .build();
    }
}
