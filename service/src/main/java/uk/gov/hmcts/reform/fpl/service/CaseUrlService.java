package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CaseUrlService {

    @Value("${manage-case.ui.base.url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCaseUrl(Long caseId) {
        return String.format("%s/cases/case-details/%s", baseUrl, caseId);
    }

    public String getCaseUrl(Long caseId, String tab) {
        String caseUrl = getCaseUrl(caseId);
        return isBlank(tab) ? caseUrl : String.format("%s#%s", caseUrl, tab);
    }
}
