package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

@Service
public class GatekeeperEmailContentProvider extends AbstractEmailContentProvider {
    private final LocalAuthorityNameLookupConfiguration config;

    @Autowired
    protected GatekeeperEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                          LocalAuthorityNameLookupConfiguration config) {
        super(uiBaseUrl);
        this.config = config;
    }

    public Map<String, Object> buildGatekeeperNotification(CaseDetails caseDetails, String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getCasePersonalisationBuilder(caseDetails.getId(), caseData)
            .put("localAuthority", config.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
