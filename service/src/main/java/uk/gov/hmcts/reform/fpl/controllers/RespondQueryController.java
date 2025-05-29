package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.RespondQueryEvent;
import uk.gov.hmcts.reform.fpl.exceptions.api.NotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getUserIdFromQueryId;
import static uk.gov.hmcts.reform.fpl.utils.QueryManagementUtils.getQueryDateFromQueryId;

@Slf4j
@RestController
@RequestMapping("/query-management")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondQueryController {
    private final EventService eventPublisher;
    private final CaseConverter caseConverter;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/query/{caseId}/3/{queryId}")
    public void sendNotificationToUser(@PathVariable String caseId,
                                     @PathVariable String queryId,
                                     @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = getCaseDetails(caseId);
        CaseData caseData = getCaseData(caseDetails);

        log.info("Going to send notification");

        eventPublisher.publishEvent(RespondQueryEvent.builder()
            .caseData(caseData)
            .queryId(queryId)
            .userId(getUserIdFromQueryId(queryId, caseDetails))
            .queryDate(getQueryDateFromQueryId(queryId, caseDetails))
            .build());
    }

    private CaseDetails getCaseDetails(String caseId) {
        try {
            return coreCaseDataService.findCaseDetailsById(caseId);
        } catch (Exception e) {
            throw new NotFoundException("Case reference not found");
        }
    }

    private CaseData getCaseData(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }
}
