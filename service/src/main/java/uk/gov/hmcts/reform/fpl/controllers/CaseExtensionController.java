package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;



@Api
@RestController
@RequestMapping("/callback/case-extension")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionController {
    private final ObjectMapper mapper;
    private LocalDate extensionDate;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        LocalDate dateSubmitted = caseData.getDateSubmitted();
        caseDetails.getData().put("shouldBeCompletedByDate", formatLocalDateToString(dateSubmitted, DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

            // Put into the label
            extensionDate = caseData.getDateSubmitted().plusWeeks(8);
            caseDetails.getData().put("extensionDate8Weeks", formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(8),
                DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if(caseDetails.getData().get("caseExtensionTimeList").equals("8WeekExtension")){
            if(caseDetails.getData().get("caseExtensionTimeConfirmationList").equals("8WeekExtension")) {
                caseDetails.getData().put("extensionDateOther", extensionDate);
            } else {
                System.out.println("date to put in" + caseDetails.getData().get("8WeeksExtensionDateOther"));
                caseDetails.getData().put("extensionDateOther", caseDetails.getData().get("8WeeksExtensionDateOther"));
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
