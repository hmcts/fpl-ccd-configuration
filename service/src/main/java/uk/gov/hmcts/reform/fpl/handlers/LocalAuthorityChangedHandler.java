package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityChangedContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityChangedHandler {

    private final NotificationService notificationService;

    private final LocalAuthorityChangedContentProvider contentProvider;

    private final ApplicantLocalAuthorityService applicantLocalAuthorityService;

    @Async
    @EventListener
    public void notifySecondaryLocalAuthority(final SecondaryLocalAuthorityRemoved event) {

        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        final String localAuthorityEmail = getSharedLocalAuthorityEmail(caseDataBefore);

        final NotifyData notifyData = contentProvider.getNotifyDataForRemovedLocalAuthority(caseData, caseDataBefore);

        notificationService.sendEmail(LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE, localAuthorityEmail,
            notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifySecondaryLocalAuthority(final SecondaryLocalAuthorityAdded event) {

        final CaseData caseData = event.getCaseData();

        final String localAuthorityEmail = getSharedLocalAuthorityEmail(caseData);

        final NotifyData notifyData = contentProvider.getNotifyDataForAddedLocalAuthority(caseData);

        notificationService.sendEmail(LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE, localAuthorityEmail,
            notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyDesignatedLocalAuthority(final SecondaryLocalAuthorityAdded event) {

        final CaseData caseData = event.getCaseData();

        final String localAuthorityEmail = getDesignatedLocalAuthorityEmail(caseData);

        final NotifyData notifyData = contentProvider.getNotifyDataForDesignatedLocalAuthority(caseData);

        notificationService.sendEmail(LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE, localAuthorityEmail,
            notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyNewDesignatedLocalAuthority(final CaseTransferred event) {

        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        final String localAuthorityEmail = getDesignatedLocalAuthorityEmail(caseData);

        final NotifyData notifyData = contentProvider.getCaseTransferredNotifyData(caseData, caseDataBefore);

        notificationService.sendEmail(CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE, localAuthorityEmail,
            notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyPreviousDesignatedLocalAuthority(final CaseTransferred event) {

        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        final String localAuthorityEmail = getDesignatedLocalAuthorityEmail(caseDataBefore);

        final NotifyData notifyData = contentProvider.getCaseTransferredNotifyData(caseData, caseDataBefore);

        notificationService.sendEmail(CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE, localAuthorityEmail,
            notifyData, caseData.getId());
    }

    private String getDesignatedLocalAuthorityEmail(CaseData caseData) {
        return applicantLocalAuthorityService.getDesignatedLocalAuthority(caseData).getEmail();
    }

    private String getSharedLocalAuthorityEmail(CaseData caseData) {
        return applicantLocalAuthorityService.getSecondaryLocalAuthority(caseData)
            .map(LocalAuthority::getEmail)
            .orElseThrow(RecipientNotFoundException::new);
    }
}
