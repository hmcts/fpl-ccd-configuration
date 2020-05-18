package uk.gov.hmcts.reform.fpl.service.email.content.base;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractEmailContentProvider {

    @Autowired
    private CaseUrlService caseUrlService;

    public String getCaseUrl(Long caseId) {
        return caseUrlService.getCaseUrl(caseId);
    }

    public String getCaseUrl(Long caseId, String tab) {
        return caseUrlService.getCaseUrl(caseId, tab);
    }
}
