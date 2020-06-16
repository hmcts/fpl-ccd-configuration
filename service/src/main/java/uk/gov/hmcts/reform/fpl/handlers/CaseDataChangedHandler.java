package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataChangedHandler {
    private final ObjectMapper mapper;
    private final CoreCaseDataService coreCaseDataService;
    private final RequestData requestData;
    private final CaseStateService caseStateService;

    @EventListener
    public void onDataChanged(final CaseDataChanged event) {

        Long caseId = event.getCallbackRequest().getCaseDetails().getId();
        CaseDetails caseDetails = coreCaseDataService.getCaseDetailsById("" + caseId);

        String caseStateInfo = caseStateService.getStatus(caseDetails);

        coreCaseDataService.triggerEvent(JURISDICTION, CASE_TYPE, caseId,"internal-XXX", Map.of("caseStateInfo", caseStateInfo));
    }


}
