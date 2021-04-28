package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeService {
    private static final String NOC_EVENT = "nocRequest";

    private final UserService userService;
    private final AuditEventService auditEventService;
    private final RespondentRepresentationService respondentRepresentationService;
    private final RespondentService respondentService;
    private final CoreCaseDataService coreCaseDataService;

    public Map<String, Object> updateRepresentation(CaseData caseData) {

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitor = userService.getUserDetailsById(auditEvent.getUserId());

        return respondentRepresentationService.updateRepresentation(caseData, solicitor);
    }

    public void updateRepresentativesAccess(CaseData caseData, CaseData caseDataBefore) {

        List<ChangeOrganisationRequest> changeRequests = respondentService
            .getRepresentationChanges(caseData.getRespondents1(), caseDataBefore.getRespondents1());

        log.info("{} representation changes detected", changeRequests.size());

        for (ChangeOrganisationRequest changeRequest : changeRequests) {
            log.info("About to apply representation change {}", changeRequest);

            coreCaseDataService.triggerEvent(caseData.getId(), "updateRepresentation",
                Map.of("changeOrganisationRequestField", changeRequest));

            log.info("Representation change applied {}", changeRequest);
        }

    }

}
