package uk.gov.hmcts.reform.fpl.service.email.content.representative;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UnregisteredRepresentativeSolicitorContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final EmailNotificationHelper helper;

    public <R extends WithSolicitor> UnregisteredRepresentativeSolicitorTemplate buildContent(CaseData caseData,
                                                                                              R representable) {
        Party party = representable.toParty();

        return UnregisteredRepresentativeSolicitorTemplate.builder()
            .ccdNumber(formatCCDCaseNumber(caseData.getId()))
            .localAuthority(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .clientFullName(isNull(party) ? EMPTY : party.getFullName())
            .caseName(caseData.getCaseName())
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }
}
