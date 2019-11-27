package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

@Service
public class InboxLookupService {
    private final ObjectMapper objectMapper;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final String fallbackInbox;

    @Autowired
    public InboxLookupService(ObjectMapper objectMapper,
                              LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                              @Value("${fpl.local_authority_fallback_inbox}") String fallbackInbox) {
        this.objectMapper = objectMapper;
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.fallbackInbox = fallbackInbox;
    }

    public String getNotificationRecipientEmail(final CaseDetails caseDetails, final String localAuthorityCode) {
        Solicitor solicitor = objectMapper.convertValue(caseDetails.getData().get("solicitor"), Solicitor.class);

        Optional<LocalAuthority> localAuthorityOptional =
            localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode);

        return localAuthorityOptional
            .map(LocalAuthority::getEmail)
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> getSolicitorOrFallbackEmail(solicitor));
    }

    private String getSolicitorOrFallbackEmail(final Solicitor solicitor) {
        return Optional.of(solicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotBlank)
            .orElse(fallbackInbox);
    }
}
