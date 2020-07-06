package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.robotics.RoboticsNotificationService;

import java.io.IOException;
import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsController {
    private static final List<String> EXCLUDED_STATES = of(OPEN.getValue(), DELETED.getValue());

    private final CoreCaseDataService coreCaseDataService;
    private final RoboticsNotificationService roboticsNotificationService;

    @PostMapping("/sendRPAEmailByID/{caseId}")
    @Secured("caseworker-publiclaw-systemupdate")
    public void resendCaseDataNotification(@PathVariable("caseId") String caseId) throws IOException {
        CaseDetails caseDetails = coreCaseDataService.findCaseDetailsById(caseId);

        performVerification(caseId, caseDetails);

        roboticsNotificationService.sendSubmittedCaseData(caseDetails);
    }

    private void performVerification(final String caseId, final CaseDetails caseDetails) {
        if (caseDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No case found with id %s", caseId));
        }

        if (EXCLUDED_STATES.contains(caseDetails.getState())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Unable to proceed as case  with id %s is in the wrong state", caseId));
        }
    }
}

