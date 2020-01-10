package uk.gov.hmcts.reform.fpl.service.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.model.ccd.CoreCaseApiSearchParameter;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
public class CoreCaseDataService {
    private final SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    public CoreCaseDataService(SystemUpdateUserConfiguration userConfig,
                               AuthTokenGenerator authTokenGenerator,
                               IdamClient idamClient,
                               CoreCaseDataApi coreCaseDataApi) {
        this.userConfig = userConfig;
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
        this.coreCaseDataApi = coreCaseDataApi;
    }

    public void triggerEvent(String jurisdiction, String caseType, Long caseId, String event) {
        String userToken = idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
        String systemUpdateUserId = idamClient.getUserDetails(userToken).getId();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                userToken,
                authTokenGenerator.generate(),
                systemUpdateUserId,
                jurisdiction,
                caseType,
                caseId.toString(),
                event);

        CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                        .id(startEventResponse.getEventId())
                        .build())
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

    public CaseDetails performCaseSearch(final String authToken, final CoreCaseApiSearchParameter apiParameter) {
        String userAuthToken = idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword());

        return coreCaseDataApi.getCase(authToken, authTokenGenerator.generate(),
            apiParameter.getCaseId().toString());
    }
}
