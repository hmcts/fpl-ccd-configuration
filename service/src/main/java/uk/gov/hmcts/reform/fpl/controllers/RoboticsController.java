package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.robotics.ResendFailedRoboticNotificationEvent;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsController {
    private final CoreCaseDataService coreCaseDataService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/sendRPAEmailByID/{caseId}")
    @Secured("caseworker-publiclaw-systemupdate")
    public void resendCaseDataNotification(@PathVariable ("caseId") String caseId) {
        CaseDetails caseDetails = coreCaseDataService.findCaseDetailsById(caseId);
        applicationEventPublisher.publishEvent(new ResendFailedRoboticNotificationEvent(caseDetails));
    }
}
