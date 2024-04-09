package uk.gov.hmcts.reform.fpl.service.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {

    public static final String UPDATE_CASE_EVENT = "internal-change-UPDATE_CASE";
    private static final int RETRIES = 3;

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final RequestData requestData;
    private final SystemUserService systemUserService;
    private final CCDConcurrencyHelper concurrencyHelper;

    public CaseDetails performPostSubmitCallbackWithoutChange(Long caseId, String eventName) {
        return concurrencyHelper.performPostSubmitCallback(caseId, eventName, (caseDetails) -> Map.of(), true);
    }

    public CaseDetails performPostSubmitCallback(Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction) {
        return concurrencyHelper.performPostSubmitCallback(caseId, eventName, changeFunction, false);
    }

    public CaseDetails performPostSubmitCallback(Long caseId,
                                          String eventName,
                                          Function<CaseDetails, Map<String, Object>> changeFunction,
                                          boolean submitIfEmpty) {
        return concurrencyHelper.performPostSubmitCallback(caseId, eventName, changeFunction, submitIfEmpty);
    }

    /**
     * Runs the UPDATE_CASE event on a given case.
     *
     * @param caseId  Case to update.
     * @param updates Map of fields to update.
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     *      data then submitEvent to submit it to avoid concurrency issues.
     */
    @Deprecated(since = "February 2023", forRemoval = false)
    public void updateCase(Long caseId, Map<String, Object> updates) {
        triggerEvent(caseId, "internal-change-UPDATE_CASE", updates);
    }

    /**
     * Triggers a CCD event on the case.
     *
     * @param caseId  Case to update.
     * @param event   CCD event name to create and submit.
     * @param updates Map of fields to update.
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     *      data then submitEvent to submit it to avoid concurrency issues.
     */
    @Deprecated(since = "February 2023", forRemoval = false)
    public void triggerEvent(Long caseId, String event, Map<String, Object> updates) {
        triggerEvent(JURISDICTION, CASE_TYPE, caseId, event, updates);
    }

    /**
     * Triggers a CCD event on the case in a given jurisdiction, casetype.
     *
     * @param jurisdiction Jurisdiction of the case in CCD
     * @param caseType     Type of the case in CCD
     * @param caseId       Case to update.
     * @param event        CCD event name to create and submit.
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     *      data then submitEvent to submit it to avoid concurrency issues.
     */
    @Deprecated(since = "February 2023", forRemoval = false)
    public void triggerEvent(String jurisdiction, String caseType, Long caseId, String event) {
        triggerEvent(jurisdiction, caseType, caseId, event, emptyMap());
    }

    /**
     * Triggers a CCD event on a case, given various params.
     *
     * @param jurisdiction Jurisdiction of the case in CCD
     * @param caseType     Type of the case in CCD
     * @param caseId       Case to update.
     * @param eventName    CCD event name to create and submit.
     * @param eventData    Map of fields to update.
     * @deprecated Method does not use CCD concurrency controls correctly, Use startEvent to retrieve current case
     *      data then submitEvent to submit it to avoid concurrency issues.
     */
    @Deprecated(since = "February 2023", forRemoval = false)
    public void triggerEvent(String jurisdiction,
                             String caseType,
                             Long caseId,
                             String eventName,
                             Map<String, Object> eventData) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        try {
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
        } catch (Exception exception) {
            log.error("Trigger event cannot be completed due to exception.", exception);
        }
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
