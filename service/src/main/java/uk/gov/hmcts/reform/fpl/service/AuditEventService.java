package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.AuditEventsResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditEventService {
    private final IdamClient idamClient;
    private final SystemUpdateUserConfiguration userConfig;
    private final CoreCaseDataApiV2 caseDataApiV2;
    private final AuthTokenGenerator authTokenGenerator;

    public Optional<AuditEvent> getLatestAuditEventByName(String caseId, String eventName) {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());

        AuditEventsResponse auditEventsResponse
            = caseDataApiV2.getAuditEvents(userToken, authTokenGenerator.generate(), false, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }
}
