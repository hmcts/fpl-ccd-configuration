package uk.gov.hmcts.reform.fpl.service.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CCDConcurrencyHelper {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final SystemUserService systemUserService;

    @Retryable(recover = "Exception.class", maxAttempts = 3, backoff = @Backoff(delay = 100))
    CaseDetails performPostSubmitCallback(Long caseId,
                                                 String eventName,
                                                 Function<CaseDetails, Map<String, Object>> changeFunction,
                                                 boolean submitIfEmpty) {

        StartEventResponse startEventResponse = this.startEvent(caseId, eventName);
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        // Work around immutable maps
        HashMap<String, Object> caseDetailsMap = new HashMap<>(caseDetails.getData());
        caseDetails.setData(caseDetailsMap);

        Map<String, Object> updates = changeFunction.apply(caseDetails);

        if (!updates.isEmpty() || submitIfEmpty) {
            log.info("Submitting event {} on case {}", eventName, caseId);
            this.submitEvent(startEventResponse, caseId, updates);
        } else {
            log.info("No updates, skipping submit event");
        }
        caseDetails.getData().putAll(updates);
        return caseDetails;
    }

    @Recover
    void recover(Exception e, String caseId) {
        log.error("All 3 retries failed to create event on ccd for case {}", caseId);
    }

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

}
