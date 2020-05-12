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

import static org.springframework.util.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;



@Api
@RestController
@RequestMapping("/callback/case-extension")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionController {
    private static final String CASE_COMPLETION_DATE = "completionDate";
    private static final String CASE_EXTENSION_DATE_LABEL = "extensionDate";
    private final ObjectMapper mapper;
    private LocalDate extensionDate;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        LocalDate dateSubmitted = caseData.getDateSubmitted();
        caseDetails.getData().put(CASE_COMPLETION_DATE, formatLocalDateToString(dateSubmitted, DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if(isEmpty(caseData.getExtensionDate())){
            extensionDate = caseData.getDateSubmitted().plusWeeks(8);
            caseDetails.getData().put(CASE_EXTENSION_DATE_LABEL, formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(8),
                DATE));
        }


//        if(!isEmpty(caseDetails.getData().get("caseExtensionConfirmationDate")))
//        {
//            System.out.println("caseExtensionConfirmationDate is not empty");
//        } else {
//            System.out.println("caseExtensionConfirmationDate is empty");
//        }
//
//        if(caseDetails.getData().get("caseExtensionTimeConfirmationList"))
//        caseDetails.getData().put("caseExtensionDate", "2019-10-10");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if(caseDetails.getData().get("caseExtensionTimeList").equals("8WeekExtension")){
            if(caseDetails.getData().get("caseExtensionTimeConfirmationList").equals("8WeekExtension")) {
                caseDetails.getData().put("caseExtensionDate", extensionDate);
            } else {
                System.out.println("date to put in" + caseDetails.getData().get("caseExtensionConfirmationDate"));
                caseDetails.getData().put("caseExtensionDate", caseDetails.getData().get("caseExtensionConfirmationDate"));
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }
}
