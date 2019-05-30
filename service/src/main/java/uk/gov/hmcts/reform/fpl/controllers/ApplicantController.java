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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api
@RestController
@RequestMapping("/callback/enter-applicant")
public class ApplicantController {

    private static final String PBA_NUMBER_CCD_KEY = "pbaNumber";

    private final MapperService mapperService;

    private static final Pattern PBA_NUMBER_REGEX = Pattern.compile("[0-9]{7}");

    private static final Pattern PBA_AT_START_REGEX = Pattern.compile("^PBA");

  //  private static final String PBA_NUMBER_CCD_KEY = "pbaNumber";

    private static final String PBA_NUMBER_FIELD_ERROR = "Payment by account (PBA) number must include 7 numbers";


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

        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);

        String remaining = pbaNumberData.substring(3);
        Matcher sevenDigits = PBA_NUMBER_REGEX.matcher(remaining);

        if (!sevenDigits.matches()) {
            errors.add(PBA_NUMBER_FIELD_ERROR);
        }

        return errors.build();
    }
}
