package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftCMOUploaded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftCMOUploadedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.DraftCMOUploadedContentProvider;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCMOUploadedEventHandler {
    private final NotificationService notificationService;
    private final DraftCMOUploadedContentProvider contentProvider;

    @Async
    @EventListener
    public void sendNotificationForJudge(final DraftCMOUploaded event) {
        CaseData caseData = event.getCaseData();
        HearingBooking hearing = event.getHearing();

        String email = null;
        AbstractJudge judge = null;

        if (isNotEmpty(hearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress())) {
            judge = hearing.getJudgeAndLegalAdvisor();
            email = hearing.getJudgeAndLegalAdvisor().getJudgeEmailAddress();
        } else if (caseData.hasAllocatedJudgeEmail()) {
            judge = caseData.getAllocatedJudge();
            email = caseData.getAllocatedJudge().getJudgeEmailAddress();
        }

        if (email == null) {
            return;
        }

        Long caseId = caseData.getId();
        DraftCMOUploadedTemplate content = contentProvider.buildTemplate(
            hearing,
            caseId,
            judge,
            caseData.getAllRespondents(),
            caseData.getFamilyManCaseNumber()
        );

        notificationService.sendEmail(
            CMO_DRAFT_UPLOADED_NOTIFICATION_TEMPLATE,
            email,
            content,
            caseId.toString()
        );
    }
}
