package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GatekeeperEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration config;

    public NotifyGatekeeperTemplate buildGatekeeperNotification(CaseData caseData) {
        NotifyGatekeeperTemplate template = buildNotifyTemplate(NotifyGatekeeperTemplate.builder().build(), caseData);

        // Check if local authority has a value otherwise use 3rd party
        if (!caseData.getCaseLocalAuthority().isEmpty()) {
            template.setLocalAuthority(config.getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        } else {
            template.setLocalAuthority(config.getLocalAuthorityName(
                caseData.getLocalAuthorities().get(0).getValue().getName())
            );
        }

        return template;
    }
}
