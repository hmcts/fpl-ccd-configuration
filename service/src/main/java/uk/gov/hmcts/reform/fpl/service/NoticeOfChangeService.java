package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeService {
    private static final String NOC_EVENT = "nocRequest";

    private final UserService userService;
    private final AuditEventService auditEventService;
    private final RespondentRepresentationService respondentRepresentationService;

    public Map<String, Object> updateRepresentation(CaseData caseData) {

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitor = userService.getUserDetailsById(auditEvent.getUserId());

        return respondentRepresentationService.updateRepresentation(caseData, solicitor);
    }
}
