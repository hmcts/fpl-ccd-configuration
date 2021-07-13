package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.AuditEvent;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.noc.UpdateRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeService {
    private static final String NOC_EVENT = "nocRequest";

    private final UserService userService;
    private final AuditEventService auditEventService;
    private final UpdateRepresentationService updateRepresentationService;
    private final RespondentService respondentService;
    private final CoreCaseDataService coreCaseDataService;

    public Map<String, Object> updateRepresentation(CaseData caseData) {

        AuditEvent auditEvent = auditEventService.getLatestAuditEventByName(caseData.getId().toString(), NOC_EVENT)
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s event in audit", NOC_EVENT)));

        UserDetails solicitor = userService.getUserDetailsById(auditEvent.getUserId());

        return updateRepresentationService.updateRepresentation(caseData, solicitor);
    }

    public void updateRepresentativesAccess(CaseData caseData, CaseData caseDataBefore,
                                            SolicitorRole.Representing representing) {

        Function<CaseData, List<Element<WithSolicitor>>> target = representing.getTarget();

        List<ChangeOrganisationRequest> changeRequests = respondentService
            .getRepresentationChanges(target.apply(caseData), target.apply(caseDataBefore), representing);

        log.info("{} representation changes detected", changeRequests.size());

        for (ChangeOrganisationRequest changeRequest : changeRequests) {
            log.info("About to apply representation change {}", changeRequest);

            coreCaseDataService.triggerEvent(caseData.getId(), "updateRepresentation",
                Map.of("changeOrganisationRequestField", changeRequest));

            log.info("Representation change applied {}", changeRequest);
        }

    }

}
