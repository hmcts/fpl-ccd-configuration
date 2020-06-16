package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/callback/enter-grounds")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroundsCallbackController {

    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/submitted")
    public void handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest){
        applicationEventPublisher.publishEvent(new CaseDataChanged(callbackrequest));
    }
}
