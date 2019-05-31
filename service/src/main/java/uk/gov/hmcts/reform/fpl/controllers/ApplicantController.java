package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    private static final String PBA_NUMBER_CCD_KEY = "pbaNumber";

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        System.out.println("START: ENTER APPLICANT: MID EVENT");
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        // if pba number is entered, ensure it always starts with PBA before its validated
        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);
        if (pbaNumberData != null) {
            System.out.println("PBA NUMBER ENTERED");
            String newPbaNumberData = PBANumberHelper.updatePBANumber(pbaNumberData);
            caseDetails.getData().put(PBA_NUMBER_CCD_KEY, newPbaNumberData);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validate(caseDetails))
            .build();
    }



    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        System.out.println("PBA NUMBER VALIDATION");
        // validate PBA number
        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);

        if (pbaNumberData != null) {
            List<String> pbaNumberErrors = PBANumberHelper.validatePBANumber(pbaNumberData);

            if (!pbaNumberErrors.isEmpty()) {
                errors.addAll(pbaNumberErrors.stream().collect(Collectors.toList()));
            }
        }

        System.out.println("END: ENTER APPLICANT: MID EVENT");
        return errors.build();
    }

}
