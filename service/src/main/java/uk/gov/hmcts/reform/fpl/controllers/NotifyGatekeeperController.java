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
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/notify-gatekeeper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyGatekeeperController {
    private final ObjectMapper mapper;
    private final ValidateGroupService validateGroupService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RequestData requestData;

    //TODO: can we validate a hearing has been added at this point? Saves some nasty exceptions in the case of
    // no hearing being present when populating standard directions FPLA-1516
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

       caseDetails.getData().put("gateKeeperEmails", resetGateKeeperEmailCollection());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validateGroupService.validateGroup(caseData, ValidateFamilyManCaseNumberGroup.class))
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(
            new PopulateStandardDirectionsEvent(callbackRequest, requestData));
        applicationEventPublisher.publishEvent(new NotifyGatekeepersEvent(callbackRequest, requestData));
    }

    private List<Element<EmailAddress>> resetGateKeeperEmailCollection() {
        return List.of(
            element(UUID.randomUUID(), EmailAddress.builder().email("").build())
        );
    }
}
