package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

import java.util.Map;

@Service
public class GatekeeperEmailContentProvider extends CaseDetailsValueMapper {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    public GatekeeperEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                          @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    public Map<String, String> buildGatekeeperNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCommon(caseDetails)
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
