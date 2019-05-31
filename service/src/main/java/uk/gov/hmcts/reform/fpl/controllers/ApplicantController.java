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
        System.out.println("START: ENTER APPLICANT: MID EVENT");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> ccdata = (Map<String, Object>) caseDetails.getData();
        for (Map.Entry<String, Object> entry : ccdata.entrySet()) {
            System.out.println("ccd data keys=" + entry.getKey());
        }

        Map<String, Object> applicantData = (Map<String, Object>) caseDetails.getData().get("applicant");
        Applicant applicant = mapperService.mapObject(applicantData, Applicant.class);
        String pbaNumberData = applicant.getPbaNumber();
        List<String> validationErrors = new ArrayList<String>();

        // if pba number is entered, ensure it always starts with PBA before its validated
        if (pbaNumberData != null) {
            System.out.println("PBA NUMBER ENTERED");
            String newPbaNumberData = PBANumberHelper.updatePBANumber(pbaNumberData);
            validationErrors = PBANumberHelper.validatePBANumber(newPbaNumberData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors)
            .build();
    }

}
