package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.service.MigrationService;

@Api
@RestController
@RequestMapping("callback/migration")
class DataMigrationController {
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final MigrationService migrationService;

    @Autowired
    public DataMigrationController(AuthTokenGenerator authTokenGenerator,
                                   CoreCaseDataApi coreCaseDataApi,
                                   MigrationService migrationService) {
        this.authTokenGenerator = authTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
        this.migrationService = migrationService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/submitted/{id}")
    public void handleAboutToSubmitEvent(
        @PathVariable("id") String caseID,
        @RequestHeader(value = "user-id") String userID,
        @RequestHeader(value = "authorization") String authorization) {

        // Init service token
        String serviceToken = this.authTokenGenerator.generate();

        // Get caseDetails from CCD
        CaseDetails caseDetails = this.coreCaseDataApi.getCase(authorization, serviceToken, caseID);

        StartEventResponse startEventResponse = this.coreCaseDataApi
            .startEventForCaseWorker(
                authorization,
                serviceToken,
                userID,
                "PUBLICLAW",
                "CARE_SUPERVISION_EPO",
                caseID,
                "enterRespondentsNew"
            );

        CaseDataContent caseData = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(migrationService.migrateCase(caseDetails).getData())
            .build();

        CaseDetails updatedCaseDetails = this.coreCaseDataApi.submitEventForCaseWorker(
            authorization, serviceToken, userID, "PUBLICLAW",
            "CARE_SUPERVISION_EPO", caseID, true, caseData);

        System.out.println(updatedCaseDetails.getCallbackResponseStatus());
    }
}
