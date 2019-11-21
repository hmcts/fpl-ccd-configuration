package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.PublicLawEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Optional;

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

    public String getLocalAuthorityOrFallbackEmail(final CaseDetails caseDetails, final String localAuthorityCode) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        return getLocalAuthorityEmail(localAuthorityCode)
            .orElse(getFallbackEmail(caseData));
    }

    private Optional<String> getLocalAuthorityEmail(final String localAuthorityCode) {
        return Optional.ofNullable(
            localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail());
    }

    private Optional<String> getSolicitorEmail(final CaseData caseData) {
        return Optional.ofNullable(caseData.getSolicitor().getEmail());
    }

    private String getFallbackEmail(final CaseData caseData) {
        return getSolicitorEmail(caseData)
            .orElse(publicLawEmailLookupConfiguration.getEmailAddress());
    }
}
