package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ChildrenUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.diff.PartyRepresentativeDiffCalculator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

        Set<RespondentSolicitor> representatives = getRepresentatives(children);

        representatives.forEach(representative -> {
            List<Child> childrenForRepresentative = getChildrenForRepresentative(children, representative);

            NotifyData notifyData = registeredContentProvider.buildContent(
                caseData, representative, childrenForRepresentative
            );

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, representative.getEmail(), notifyData, caseData.getId()
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

        Set<RespondentSolicitor> representatives = getRepresentatives(children);

        representatives.forEach(representative -> {
            List<Child> childrenForRepresentative = getChildrenForRepresentative(children, representative);

            NotifyData notifyData = unregisteredContentProvider.buildContent(caseData, childrenForRepresentative);

            notificationService.sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, representative.getEmail(), notifyData, caseData.getId()
            );
        });
    }

    private Set<RespondentSolicitor> getRepresentatives(List<Child> children) {
        return children.stream().map(Child::getSolicitor).collect(Collectors.toSet());
    }

    private List<Child> getChildrenForRepresentative(List<Child> children, RespondentSolicitor representative) {
        return children.stream()
            .filter(child -> Objects.equals(child.getSolicitor(), representative))
            .collect(Collectors.toList());
    }
}
