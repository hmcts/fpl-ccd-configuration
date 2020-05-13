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
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.time.LocalDate;

import static org.springframework.util.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;


@Api
@RestController
@RequestMapping("/callback/case-extension")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;
    private final String CASE_COMPLETION_DATE = "caseCompletionDate";
    private LocalDate caseCompletionDate;
    private LocalDate eightWeekExtensionDate;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if(isEmpty(caseData.getCaseCompletionDate())){
            caseCompletionDate = caseData.getDateSubmitted();
        } else {
            caseCompletionDate = caseData.getCaseCompletionDate();
        }

        caseDetails.getData().put("shouldBeCompletedByDate", formatLocalDateToString(caseCompletionDate, DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        eightWeekExtensionDate = caseCompletionDate.plusWeeks(8);
        caseDetails.getData().put("extensionDateEightWeeks", formatLocalDateToString(eightWeekExtensionDate,
                DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, CaseExtensionGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseCompletionDate = getCaseCompletionDate(caseDetails);
        caseDetails.getData().put(CASE_COMPLETION_DATE, caseCompletionDate);


        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private LocalDate getCaseCompletionDate(CaseDetails caseDetails){
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if(caseDetails.getData().get("caseExtensionTimeList").equals("EightWeekExtension")){
            if(caseDetails.getData().get("caseExtensionTimeConfirmationList").equals("EightWeekExtension")) {
               return eightWeekExtensionDate;
            } else {
                return caseData.getEightWeeksExtensionDateOther();
            }
        } else {
           return caseData.getExtensionDateOther();
        }
    }
}
