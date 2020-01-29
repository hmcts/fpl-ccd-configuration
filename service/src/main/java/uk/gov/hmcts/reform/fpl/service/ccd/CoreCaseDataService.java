package uk.gov.hmcts.reform.fpl.service.ccd;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Map;

import static java.util.Collections.emptyMap;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final RequestData requestData;

    public void triggerEvent(String jurisdiction, String caseType, Long caseId, String event) {
        triggerEvent(jurisdiction, caseType, caseId, event, emptyMap());
    }

    //TODO fix text shouldMakeAppropriateApiCalls()
    public void triggerEvent(String jurisdiction,
                             String caseType,
                             Long caseId,
                             String eventName,
                             Map<String, Object> eventData) {
        String userToken = idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
        String systemUpdateUserId = idamClient.getUserDetails(userToken).getId();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            jurisdiction,
            caseType,
            caseId.toString(),
            eventName);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(eventData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            jurisdiction,
            caseType,
            caseId.toString(),
            true,
            caseDataContent);
    }

    public CaseDetails findCaseDetailsById(final String caseId) {
        return coreCaseDataApi.getCase(requestData.authorisation(), authTokenGenerator.generate(), caseId);
    }
}
