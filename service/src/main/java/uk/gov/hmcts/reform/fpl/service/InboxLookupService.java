package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.PublicLawEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
public class InboxLookupService {
    private final ObjectMapper objectMapper;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final PublicLawEmailLookupConfiguration publicLawEmailLookupConfiguration;

    public InboxLookupService(ObjectMapper objectMapper,
                              LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                              PublicLawEmailLookupConfiguration publicLawEmailLookupConfiguration) {
        this.objectMapper = objectMapper;
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.publicLawEmailLookupConfiguration = publicLawEmailLookupConfiguration;
    }

    public String getNotificationRecipientEmail(final CaseDetails caseDetails, final String localAuthorityCode) {
        Solicitor solicitor = objectMapper.convertValue(caseDetails.getData().get("solicitor"), Solicitor.class);

        return getLocalAuthorityEmail(localAuthorityCode)
            .orElse(getFallbackEmail(solicitor));
    }

    private Optional<String> getLocalAuthorityEmail(final String localAuthorityCode) {
        return isNotBlank(
            localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail())
            ? Optional.of(localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail())
            : Optional.empty();
    }

    private Optional<String> getSolicitorEmail(final Solicitor solicitor) {
        return isNotBlank(solicitor.getEmail())
            ? Optional.of(solicitor.getEmail())
            : Optional.empty();
    }

    private String getFallbackEmail(final Solicitor solicitor) {
        return getSolicitorEmail(solicitor)
            .orElse(publicLawEmailLookupConfiguration.getEmailAddress());
    }
}
