package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.GeneralFplaEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class GenericInboxLookupService {

    private final ObjectMapper objectMapper;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private GeneralFplaEmailLookupConfiguration generalFplaEmailLookupConfiguration;

    public GenericInboxLookupService(ObjectMapper objectMapper,
                                     LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration,
                                     GeneralFplaEmailLookupConfiguration generalFplaEmailLookupConfiguration) {
        this.objectMapper = objectMapper;
        this.localAuthorityEmailLookupConfiguration = localAuthorityEmailLookupConfiguration;
        this.generalFplaEmailLookupConfiguration = generalFplaEmailLookupConfiguration;
    }

    public String getEmail(CaseDetails caseDetails, String localAuthorityCode) {
        String email = localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode).getEmail();
        if (isEmpty(email)) {
            CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
            email = isNotEmpty(caseData.getSolicitor().getEmail())
                ? caseData.getSolicitor().getEmail() : generalFplaEmailLookupConfiguration.getGeneralFplaInbox();
        }
        return email;
    }
}
