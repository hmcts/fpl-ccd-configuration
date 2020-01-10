package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ccd.CoreCaseApiSearchParameter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsController {
    public static final String JURISDICTION_ID = "PUBLICLAW";
    public static final String CASE_TYPE_ID = "CARE_SUPERVISION_EPO";

    private final CoreCaseDataService coreCaseDataService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper mapper;

    @PostMapping("/sendRPAEmailByID/{caseId}")
    @Secured("caseworker-publiclaw-systemupdate")
    public void resendCaseDataNotification(@PathVariable ("caseId") String caseId) {
        CoreCaseApiSearchParameter coreCaseSearchParameter = CoreCaseApiSearchParameter.builder()
            .caseId(caseId)
            .caseType(CASE_TYPE_ID)
            .jurisdiction(JURISDICTION_ID)
            .build();

        CaseDetails caseDetails = coreCaseDataService.performCaseSearch(coreCaseSearchParameter);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        applicationEventPublisher.publishEvent(new ResendFailedRoboticNotificationEvent(caseData));
    }
}
