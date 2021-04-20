package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.AuditEventsResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/check-noc-approval")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
// TODO
// May need to rename
public class NoticeOfChangeController extends CallbackController {
    private final IdamClient idamClient;
    private final RequestData requestData;
    private final CoreCaseDataApiV2 coreCaseDataApiV2;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUpdateUserConfiguration userConfig;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());

        AuditEventsResponse auditEvents = coreCaseDataApiV2.getAuditEvents(userToken,
            authTokenGenerator.generate(), false, caseDetails.getId().toString());

        Optional<AuditEvent> nocRequestAuditEvent
            = getAuditEventByEventId(auditEvents.getAuditEvents(), "nocRequest");

        if (nocRequestAuditEvent.isPresent()) {
            UserDetails userDetails = idamClient.getUserByUserId(requestData.authorisation(),
                nocRequestAuditEvent.get().getUserId());

            caseDetailsMap.put("NoCUser", userDetails);
        } else {
            // throw exception
        }

        return respond(caseDetailsMap);
    }

    private Optional<AuditEvent> getAuditEventByEventId(List<AuditEvent> auditEventList, String eventId) {
        return auditEventList.stream()
            .filter(auditEvent -> eventId.equals(auditEvent.getId()))
            .findFirst();
    }
}
