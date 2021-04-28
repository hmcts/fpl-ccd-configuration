package uk.gov.hmcts.reform.fpl.testingsupport.controllers;

import feign.FeignException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnExpression("${testing.support.enabled:false}")
@SuppressWarnings("unchecked")
public class TestingSupportController {
    private static final String POPULATE_EVENT_ID_TEMPLATE = "populateCase-%s";
    private final CoreCaseDataService coreCaseDataService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final CoreCaseDataApiV2 coreCaseDataApiV2;
    private final RequestData requestData;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    @PostMapping(value = "/testing-support/case/create", produces = APPLICATION_JSON_VALUE)
    public Map createCase(@RequestBody Map<String, Object> requestBody) {

        StartEventResponse startEventResponse = coreCaseDataApi.startCase(
            requestData.authorisation(),
            authTokenGenerator.generate(),
            CASE_TYPE,
            "openCase");

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(requestBody)
            .build();

        return coreCaseDataApiV2.saveCase(
            requestData.authorisation(),
            authTokenGenerator.generate(),
            CASE_TYPE,
            caseDataContent);
    }

    @PostMapping("/testing-support/case/populate/{caseId}")
    public void populateCase(@PathVariable("caseId") Long caseId, @RequestBody Map<String, Object> requestBody) {
        State state = State.fromValue(requestBody.get("state").toString());
        Map<String, Object> caseData = (Map<String, Object>) requestBody.get("caseData");

        try {
            coreCaseDataService.triggerEvent(JURISDICTION,
                CASE_TYPE,
                caseId,
                String.format(POPULATE_EVENT_ID_TEMPLATE, state.getValue()),
                caseData);
        } catch (FeignException e) {
            log.error(String.format("Populate case event failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    @PostMapping("/testing-support/user")
    public UserDetails getUser(@RequestBody Map<String, String> requestBody) {
        final String token = idamClient.getAccessToken(requestBody.get("email"), requestBody.get("password"));
        return idamClient.getUserDetails(token);
    }
}
