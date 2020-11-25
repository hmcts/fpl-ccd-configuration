package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.notify.LegalRepresentativeAddedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalRepresentativeAddedContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration nameLookupConfiguration;

    public LegalRepresentativeAddedTemplate getNotifyData(LegalRepresentative legalRepresentative,
                                                          CaseData caseData) {
        return LegalRepresentativeAddedTemplate.builder()
            .repName(legalRepresentative.getFullName())
            .localAuthority(nameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .firstRespondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }

}
