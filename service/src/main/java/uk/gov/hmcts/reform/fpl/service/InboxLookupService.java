package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InboxLookupService {
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final FeatureToggleService featureToggleService;

    @Value("${fpl.local_authority_fallback_inbox}")
    private String fallbackInbox;

    public Set<String> getRecipients(final CaseData caseData) {
        Set<String> recipients = new HashSet<>();
        localAuthorityEmailLookupConfiguration.getLocalAuthority(caseData.getCaseLocalAuthority())
            .map(LocalAuthority::getEmail)
            .filter(StringUtils::isNotBlank)
            .ifPresent(recipients::add);

        if (recipients.isEmpty() || featureToggleService.isSendLAEmailsToSolicitorEnabled(
            caseData.getCaseLocalAuthority())) {
            Optional.ofNullable(caseData.getSolicitor())
                .map(Solicitor::getEmail)
                .filter(StringUtils::isNotBlank)
                .ifPresent(recipients::add);
        }

        if (recipients.isEmpty()) {
            recipients.add(fallbackInbox);
        }

        return recipients;
    }
}
