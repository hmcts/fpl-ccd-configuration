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
import uk.gov.hmcts.reform.fpl.events.CaseSubmittedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

@Api
@RestController
@RequestMapping("/callback/add-case-number")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddCaseNumberController {
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isAlphanumeric(caseData.getFamilyManCaseNumber())) {
            caseDetails.getData().keySet().removeIf(key -> key.equalsIgnoreCase("familyManCaseNumber"));

            String invalidFamilymanCaseNumberErrorMessage = "Enter a valid FamilyMan case number";
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(singletonList(invalidFamilymanCaseNumberErrorMessage))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);
        applicationEventPublisher.publishEvent(new CaseSubmittedEvent(caseData));
    }
}
