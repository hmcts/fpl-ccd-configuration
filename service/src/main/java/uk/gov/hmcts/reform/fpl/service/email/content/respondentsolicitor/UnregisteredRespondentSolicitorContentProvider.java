package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.UnregisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UnregisteredRespondentSolicitorContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final EmailNotificationHelper helper;

    public UnregisteredRespondentSolicitorTemplate buildContent(CaseData caseData,
                                                                Respondent respondent) {
        String respondentName = isNull(respondent.getParty()) ? EMPTY : respondent.getParty().getFullName();

        return UnregisteredRespondentSolicitorTemplate.builder()
            .ccdNumber(formatCCDCaseNumber(caseData.getId()))
            .localAuthority(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .clientFullName(respondentName)
            .caseName(caseData.getCaseName())
            .childLastName(helper.getEldestChildLastName(caseData.getChildren1()))
            .build();
    }
}
