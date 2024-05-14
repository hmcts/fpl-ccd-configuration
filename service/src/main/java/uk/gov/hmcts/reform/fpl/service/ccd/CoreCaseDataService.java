package uk.gov.hmcts.reform.fpl.service.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.exceptions.RetryFailureException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Resource;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoreCaseDataService {

    public static final String UPDATE_CASE_EVENT = "internal-change-UPDATE_CASE";

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final RequestData requestData;
    private final SystemUserService systemUserService;
    private final CCDConcurrencyHelper concurrencyHelper;

    // Required so calls to the same class get proxied correctly and have the retry annotation applied
    @Lazy
    @Resource(name = "coreCaseDataService")
    private CoreCaseDataService self;

    public CaseDetails performPostSubmitCallbackWithoutChange(Long caseId, String eventName) {
        return self.performPostSubmitCallback(caseId, eventName, (caseDetails) -> Map.of(), true);
    }

    public CaseDetails performPostSubmitCallback(Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction) {
        return self.performPostSubmitCallback(caseId, eventName, changeFunction, false);
    }

    @Retryable(recover = "recover", maxAttempts = 5, backoff = @Backoff(delay = 2000))
    public CaseDetails performPostSubmitCallback(Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction,
                                                 boolean submitIfEmpty) {

        StartEventResponse startEventResponse = concurrencyHelper.startEvent(caseId, eventName);
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        // Work around immutable maps
        HashMap<String, Object> caseDetailsMap = new HashMap<>(caseDetails.getData());
        caseDetails.setData(caseDetailsMap);

        Map<String, Object> updates = changeFunction.apply(caseDetails);

        if (!updates.isEmpty() || submitIfEmpty) {
            log.info("Submitting event {} on case {}", eventName, caseId);
            concurrencyHelper.submitEvent(startEventResponse, caseId, updates);
        } else {
            log.info("No updates, skipping submit event");
        }
        caseDetails.getData().putAll(updates);
        return caseDetails;
    }

    @Recover
    void recover(Exception e, Long caseId, String eventName,
                 Function<CaseDetails, Map<String, Object>> changeFunction,
                 boolean submitIfEmpty) {
        throw new RetryFailureException(
            String.format("All retries failed to create event %s on ccd for case %d", eventName, caseId), e);
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
