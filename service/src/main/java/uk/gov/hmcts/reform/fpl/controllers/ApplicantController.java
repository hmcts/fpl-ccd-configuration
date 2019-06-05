package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    @Autowired
    private MapperService mapperService;

    @SuppressWarnings("unchecked")
    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> applicantData = (Map<String, Object>) caseDetails.getData().get("applicant");
        Applicant applicant = mapperService.mapObject(applicantData, Applicant.class);
        List<String> validationErrors = new ArrayList<String>();

        if (applicant.getPbaNumber() != null) {
            String newPbaNumberData = PBANumberHelper.updatePBANumber(applicant.getPbaNumber());
            validationErrors = PBANumberHelper.validatePBANumber(newPbaNumberData);
            if (validationErrors.isEmpty()) {
                applicantData.put("pbaNumber", newPbaNumberData);
                caseDetails.getData().put("applicant", applicantData);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors)
            .build();
    }

}
