package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.AuditEventsResponse;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuditEventService {
    private final CoreCaseDataApiV2 caseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;

    public Optional<AuditEvent> getLatestAuditEventByName(String caseId, String eventName) {
        String userToken = systemUserService.getSysUserToken();

        AuditEventsResponse auditEventsResponse
            = caseDataApi.getAuditEvents(userToken, authTokenGenerator.generate(), false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }

    public Optional<AuditEvent> getOldestAuditEventByName(String caseId, String eventName) {
        log.info("Case id {} and event name {}", caseId, eventName);
        String userToken = systemUserService.getSysUserToken();

        AuditEventsResponse auditEventsResponse
            = caseDataApi.getAuditEvents(userToken, authTokenGenerator.generate(), false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
                .peek(auditEvent -> log.info("audit event name {}", auditEvent.getId()))
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .min(Comparator.comparing(AuditEvent::getCreatedDate));
    }
}
