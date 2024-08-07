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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.exceptions.RetryFailureException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

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
