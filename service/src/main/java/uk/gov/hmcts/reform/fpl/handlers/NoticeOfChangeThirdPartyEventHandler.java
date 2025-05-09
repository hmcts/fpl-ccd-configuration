package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.NoticeOfChangeThirdPartyEvent;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfChangeContentProvider;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeThirdPartyEventHandler {

    private final NotificationService notificationService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;
    private final OrganisationService organisationService;

    @Async
    @EventListener
    public void notifyThirdPartySolicitorAccessGranted(final NoticeOfChangeThirdPartyEvent event) {
        NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(
            event.getCaseData());

        String email = getEmail(event.getNewThirdPartyOrg());

        notificationService.sendEmail(NOTICE_OF_CHANGE_NEW_REPRESENTATIVE, email,
            notifyData, event.getCaseData().getId());
    }

    @Async
    @EventListener
    public void notifyThirdPartySolicitorAccessRemoved(final NoticeOfChangeThirdPartyEvent event) {
        NotifyData notifyData = noticeOfChangeContentProvider.buildNoticeOfChangeThirdPartySolicitorTemplate(
            event.getCaseData());

        String email = getEmail(event.getOldThirdPartyOrg());

        notificationService.sendEmail(
            NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
            email,
            notifyData, event.getCaseData().getId()
        );
    }

    private String getEmail(LocalAuthority thirdPartyOrg) {
        if (isEmpty(thirdPartyOrg.getEmail())) {
            Organisation organisation = organisationService.findOrganisation(thirdPartyOrg.getId()).orElseThrow();
            return organisation.getSuperUser().getEmail();
        }
        return thirdPartyOrg.getEmail();
    }
}
