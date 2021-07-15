package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ChildrenUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.diff.PartyRepresentativeDiffCalculator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ChildrenUpdatedEventHandler {
    private final RegisteredRepresentativeSolicitorContentProvider registeredContentProvider;
    private final UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider;
    private final PartyRepresentativeDiffCalculator<Child> diffCalculator;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void notifyRegisteredSolicitors(final ChildrenUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<Child> children = diffCalculator.getRegisteredDiff(
            caseData.getAllChildren(), caseDataBefore.getAllChildren()
        );

        children.forEach(child -> {
            NotifyData notifyData = registeredContentProvider.buildContent(caseData, child);

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, child.getSolicitor().getEmail(), notifyData, caseData.getId()
            );
        });
    }

    @Async
    @EventListener
    public void notifyUnRegisteredSolicitors(final ChildrenUpdated event) {
        CaseData caseData = event.getCaseData();
        CaseData caseDataBefore = event.getCaseDataBefore();

        List<Child> children = diffCalculator.getUnregisteredDiff(
            caseData.getAllChildren(), caseDataBefore.getAllChildren()
        );

        children.forEach(child -> {
            NotifyData notifyData = unregisteredContentProvider.buildContent(caseData, child);

            notificationService.sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, child.getSolicitor().getEmail(), notifyData,
                caseData.getId()
            );
        });
    }

}
