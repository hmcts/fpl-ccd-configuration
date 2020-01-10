package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.events.robotics.ResendFailedRoboticNotificationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ccd.CoreCaseApiSearchParameter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

@Api
@RestController
@RequestMapping("/callback/add-case-number")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddCaseNumberController {
    public static final String JURISDICTION_ID = "PUBLICLAW";
    public static final String CASE_TYPE_ID = "CARE_SUPERVISION_EPO";

    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors(caseDetails))
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        CoreCaseApiSearchParameter caseApiParameter = CoreCaseApiSearchParameter.builder()
            .caseId(callbackRequest.getCaseDetails().getId().toString())
            .caseType(CASE_TYPE_ID)
            .jurisdiction(JURISDICTION_ID)
            .build();

        coreCaseDataService.performCaseSearch(caseApiParameter);
        applicationEventPublisher.publishEvent(new CaseNumberAdded(caseData));
        applicationEventPublisher.publishEvent(new ResendFailedRoboticNotificationEvent(caseData));
    }

    private List<String> validationErrors(final CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isAlphanumeric(caseData.getFamilyManCaseNumber())) {
            return singletonList("Enter a valid FamilyMan case number");
        }

        return emptyList();
    }
}
