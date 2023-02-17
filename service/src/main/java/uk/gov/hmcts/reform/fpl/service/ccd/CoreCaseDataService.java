package uk.gov.hmcts.reform.fpl.service.ccd;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final RequestData requestData;
    private final SystemUserService systemUserService;

    public StartEventResponse startEvent(Long caseId, String eventName) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        return coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            caseId.toString(),
            eventName);
    }

    public void submitEvent(StartEventResponse startEventResponse, Long caseId, Map<String, Object> eventData) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

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
            JURISDICTION,
            CASE_TYPE,
            caseId.toString(),
            true,
            caseDataContent);

    }

    /**
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     * data then submitEvent to submit it to avoid concurrency issues.
     * @param caseId Case to update.
     * @param updates Map of fields to update.
     */
    @Deprecated(since = "February 2023", forRemoval = true)
    public void updateCase(Long caseId, Map<String, Object> updates) {
        //
        triggerEvent(caseId, "internal-change-UPDATE_CASE", updates);
    }

    /**
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     * data then submitEvent to submit it to avoid concurrency issues.
     * @param caseId Case to update.
     * @param event CCD event name to create and submit.
     * @param updates Map of fields to update.
     */
    @Deprecated(since = "February 2023", forRemoval = true)
    public void triggerEvent(Long caseId, String event, Map<String, Object> updates) {
        triggerEvent(JURISDICTION, CASE_TYPE, caseId, event, updates);
    }

    /**
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     * data then submitEvent to submit it to avoid concurrency issues.
     * @param jurisdiction Jurisdiction of the case in CCD
     * @param caseType Type of the case in CCD
     * @param caseId Case to update.
     * @param event CCD event name to create and submit.
     */
    @Deprecated(since = "February 2023", forRemoval = true)
    public void triggerEvent(String jurisdiction, String caseType, Long caseId, String event) {
        triggerEvent(jurisdiction, caseType, caseId, event, emptyMap());
    }

    /**
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     * data then submitEvent to submit it to avoid concurrency issues.
     * @param jurisdiction Jurisdiction of the case in CCD
     * @param caseType Type of the case in CCD
     * @param caseId Case to update.
     * @param eventName CCD event name to create and submit.
     * @param eventData Map of fields to update.
     */
    @Deprecated(since = "February 2023", forRemoval = true)
    public void triggerEvent(String jurisdiction,
                             String caseType,
                             Long caseId,
                             String eventName,
                             Map<String, Object> eventData) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

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

    public CaseDetails findCaseDetailsByIdNonUser(final String caseId) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), CASE_TYPE,
            new MatchQuery("reference", caseId).toQueryContext(1, 0).toString()).getCases().get(0);
    }

    public SearchResult searchCases(String caseType, String query) {
        String userToken = systemUserService.getSysUserToken();

        return coreCaseDataApi.searchCases(userToken, authTokenGenerator.generate(), caseType, query);
    }
}
