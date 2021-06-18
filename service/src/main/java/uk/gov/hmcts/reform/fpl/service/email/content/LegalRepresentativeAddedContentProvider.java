package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.notify.LegalRepresentativeAddedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LegalRepresentativeAddedContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration nameLookupConfiguration;
    private final EmailNotificationHelper helper;

    public LegalRepresentativeAddedTemplate getNotifyData(LegalRepresentative legalRepresentative,
                                                          CaseData caseData) {
        return LegalRepresentativeAddedTemplate.builder()
            .repName(legalRepresentative.getFullName())
            .localAuthority(nameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .firstRespondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .caseUrl(getCaseUrl(caseData.getId()))
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }

}
