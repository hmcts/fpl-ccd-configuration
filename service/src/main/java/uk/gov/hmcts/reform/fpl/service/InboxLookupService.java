package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

@Service
public class InboxLookupService {
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final String fallbackInbox;

    @Autowired
    public InboxLookupService(LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                              @Value("${fpl.local_authority_fallback_inbox}") String fallbackInbox) {
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.fallbackInbox = fallbackInbox;
    }

    public String getNotificationRecipientEmail(final CaseData caseData) {
        return localAuthorityEmailLookupConfiguration.getLocalAuthority(caseData.getCaseLocalAuthority())
            .map(LocalAuthority::getEmail)
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> getSolicitorOrFallbackEmail(caseData.getSolicitor()));
    }

    private String getSolicitorOrFallbackEmail(final Solicitor solicitor) {
        return Optional.ofNullable(solicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotBlank)
            .orElse(fallbackInbox);
    }
}
