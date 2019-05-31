package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    private static final String PBA_NUMBER_CCD_KEY = "pbaNumber";

    private final MapperService mapperService;

    @Autowired
    public ApplicantController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        // if pba number is entered, ensure it always starts with PBA before its validated
        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);
        if (pbaNumberData != null) {
            String newPbaNumberData = PBANumberHelper.updatePBANumber(caseDetails);
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

        // validate PBA number
        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);
        List<String> pbaNumberErrors = PBANumberHelper.validatePBANumber(pbaNumberData);

        if (!pbaNumberErrors.isEmpty()) {
            errors.addAll(pbaNumberErrors.stream().collect(Collectors.toList()));
        }

        return errors.build();
    }

}
