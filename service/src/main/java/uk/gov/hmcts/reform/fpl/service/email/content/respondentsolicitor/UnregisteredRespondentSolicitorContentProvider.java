package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.UnregisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UnregisteredRespondentSolicitorContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final CaseDetailsHelper caseHelper;

    public UnregisteredRespondentSolicitorTemplate buildContent(CaseData caseData) {
        return UnregisteredRespondentSolicitorTemplate.builder()
            .ccdCode(caseHelper.formatCCDCaseNumber(caseData.getId()))
            .localAuthority(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .build();
    }
}
