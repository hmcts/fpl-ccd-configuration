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

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        LocalDate caseCompletionDate = getCaseCompletionOrSubmittedDate(caseData);

        caseDetails.getData().put("shouldBeCompletedByDate", formatLocalDateToString(caseCompletionDate, DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        LocalDate eightWeekExtensionDate = getCaseCompletionDateFor8WeekExtension(caseData);

        caseDetails.getData().put("extensionDateEightWeeks", formatLocalDateToString(eightWeekExtensionDate, DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, CaseExtensionGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        LocalDate caseCompletionDate = getCaseCompletionDate(caseDetails);
        caseDetails.getData().put("caseCompletionDate", caseCompletionDate);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private LocalDate getCaseCompletionDate(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseDetails.getData().get("caseExtensionTimeList").equals("EightWeekExtension")) {
            if (caseDetails.getData().get("caseExtensionTimeConfirmationList").equals("EightWeekExtension")) {
                return getCaseCompletionDateFor8WeekExtension(caseData);
            }
            return caseData.getEightWeeksExtensionDateOther();
        }
        return caseData.getExtensionDateOther();
    }

    private LocalDate getCaseCompletionDateFor8WeekExtension(CaseData caseData) {
        return getCaseCompletionOrSubmittedDate(caseData).plusWeeks(8);
    }

    private LocalDate getCaseCompletionOrSubmittedDate(CaseData caseData) {
        if (isEmpty(caseData.getCaseCompletionDate())) {
            return caseData.getDateSubmitted();
        } else {
            return caseData.getCaseCompletionDate();
        }
    }
}
