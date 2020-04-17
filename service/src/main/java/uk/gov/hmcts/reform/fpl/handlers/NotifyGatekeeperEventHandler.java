package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyGatekeeperEventHandler {
    private final NotificationService notificationService;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final ObjectMapper mapper;

    @EventListener
    public void sendEmailToGatekeeper(NotifyGatekeepersEvent event) {
        EventData eventData = new EventData(event);
        CaseData caseData = mapper.convertValue(eventData.getCaseDetails().getData(), CaseData.class);

        Map<String, Object> commonParameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode());

        List<String> emailList = getDistinctGatekeeperEmails(caseData.getGateKeeperEmails());

        emailList.forEach(recipientEmail -> {
            Map<String, Object> parameters = new HashMap<>(commonParameters);

            parameters.put("gatekeeper_recipients",
                gatekeeperEmailContentProvider.buildRecipientsLabel(emailList, recipientEmail));

            notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, recipientEmail, parameters,
                eventData.getReference());
        });
    }

    private List<String> getDistinctGatekeeperEmails(List<Element<EmailAddress>> emailCollection) {
        return unwrapElements(emailCollection)
            .stream()
            .distinct()
            .map(EmailAddress::getEmail)
            .collect(Collectors.toList());
    }
}
