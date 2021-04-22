package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeService {
    private static final String NOC_EVENT = "nocRequest";

    private final UserService userService;
    private final AuditEventService auditEventService;
    private final RespondentPolicyService respondentPolicyService;

    public List<Element<Respondent>> updateRepresentation(CaseData caseData) {

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s in audit", NOC_EVENT)));

        log.info("Audit event found {}", auditEvent);

        log.info("Audit event user {}", auditEvent.getUserId());

        UserDetails solicitor = userService.getUserDetailsById(auditEvent.getUserId());

        log.info("Audit event user details test {}", solicitor);

        return respondentPolicyService.updateNoticeOfChangeRepresentation(caseData, solicitor);
    }
}
