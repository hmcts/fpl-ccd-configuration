package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CaseUrlService {

    private final FeatureToggleService featureToggleService;

    @Value("${ccd.ui.base.url}")
    private String ccdBaseUrl;

    @Value("${manage-case.ui.base.url}")
    private String xuiBaseUrl;

    public String getCaseUrl(Long caseId) {
        if (featureToggleService.isExpertUIEnabled()) {
            return String.format("%s/cases/case-details/%s", xuiBaseUrl, caseId);
        } else {
            return String.format("%s/case/%s/%s/%s", ccdBaseUrl, JURISDICTION, CASE_TYPE, caseId);
        }
    }

    public String getCaseUrl(Long caseId, String tab) {
        String caseUrl = getCaseUrl(caseId);
        return isBlank(tab) ? caseUrl : String.format("%s#%s", caseUrl, tab);
    }
}
