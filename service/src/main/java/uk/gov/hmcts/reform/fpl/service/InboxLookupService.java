package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
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
            .filter(localAuthority -> !localAuthority.getEmail().isEmpty())
            .map(LocalAuthority::getEmail)
            .orElseGet(() -> getFallbackEmail(solicitor));
    }

    private String getSolicitorEmail(final Solicitor solicitor) {
        return defaultIfBlank(solicitor.getEmail(), "");
    }

    private String getFallbackEmail(final Solicitor solicitor) {
        return defaultIfBlank(getSolicitorEmail(solicitor), fallbackInbox);
    }
}
