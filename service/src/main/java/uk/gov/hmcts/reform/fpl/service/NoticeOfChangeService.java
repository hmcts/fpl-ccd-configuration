package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeService {
    private final AuditEventService auditEventService;
    private final RespondentPolicyService respondentPolicyService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private static final String NOC_EVENT = "nocRequest";

    public List<Element<Respondent>> updateRespondentsOnNoc(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = objectMapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        Optional<AuditEvent> nocRequestAuditEvent
            = auditEventService.getLatestAuditEventByName(caseDetails.getId().toString(), NOC_EVENT);

        AuditEvent auditEvent = nocRequestAuditEvent.orElseThrow(() -> new IllegalStateException(
            String.format("Could not find an occurrence of %s in audit events", NOC_EVENT)));

        UserDetails userDetails = userService.getUserDetailsById(auditEvent.getUserId());

        return respondentPolicyService.updateRespondentPolicies(caseData, caseDataBefore, userDetails);
    }
}
