package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.UpcomingHearingsFound;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.UpcomingHearingsContentProvider;

import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UPCOMING_HEARINGS_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpcomingHearingsFoundNotificationHandler {
    private final NotificationService notificationService;
    private final FeatureToggleService featureToggleService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final UpcomingHearingsContentProvider upcomingHearingsEmailContentProvider;

    @EventListener
    public void sendEmailWithUpcomingHearings(UpcomingHearingsFound event) {
        if (featureToggleService.isCtscReportEnabled()) {
            if (!isEmpty(event.getCaseDetails())) {
                Map<String, Object> parameters = upcomingHearingsEmailContentProvider.buildParameters(
                    event.getHearingDate(), event.getCaseDetails());
                String email = ctscEmailLookupConfiguration.getEmail();
                String reference = event.getHearingDate().toString();

                notificationService.sendEmail(UPCOMING_HEARINGS_TEMPLATE, email, parameters, reference);
            } else {
                log.info("Email of upcoming hearings not sent as no cases to be heard on {}", event.getHearingDate());
            }
        } else {
            log.info("Sending email of upcoming hearings is turned off");
        }
    }
}
