package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.NotifyAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;

@Api
@RestController
@RequestMapping("/callback/allocated-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RequestData requestData;

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        applicationEventPublisher.publishEvent(new NotifyAllocatedJudgeEvent(callbackRequest, requestData));
    }
}
