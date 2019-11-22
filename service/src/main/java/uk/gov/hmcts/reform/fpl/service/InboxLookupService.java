package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.DefaultEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

@Service
public class InboxLookupService {
    private final ObjectMapper objectMapper;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final DefaultEmailLookupConfiguration defaultEmailLookupConfiguration;

    @Autowired
    public InboxLookupService(ObjectMapper objectMapper,
                              LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                              DefaultEmailLookupConfiguration defaultEmailLookupConfiguration) {
        this.objectMapper = objectMapper;
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.defaultEmailLookupConfiguration = defaultEmailLookupConfiguration;
    }

    public String getNotificationRecipientEmail(final CaseDetails caseDetails, final String localAuthorityCode) {
        Solicitor solicitor = objectMapper.convertValue(caseDetails.getData().get("solicitor"), Solicitor.class);

        return defaultIfBlank(localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail(),
            getFallbackEmail(solicitor));
    }

    private String getSolicitorEmail(final Solicitor solicitor) {
        return defaultIfBlank(solicitor.getEmail(), "");
    }

    private String getFallbackEmail(final Solicitor solicitor) {
        return defaultIfBlank(getSolicitorEmail(solicitor), defaultEmailLookupConfiguration.getEmailAddress());
    }
}
