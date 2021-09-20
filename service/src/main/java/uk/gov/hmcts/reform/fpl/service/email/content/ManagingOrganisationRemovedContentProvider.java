package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ManagingOrganisationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.rd.model.Organisation;

@Service
public class ManagingOrganisationRemovedContentProvider {

    public NotifyData getEmailData(Organisation managingOrganisation, CaseData caseData) {
        return ManagingOrganisationRemovedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .caseNumber(caseData.getId())
            .localAuthorityName(caseData.getCaseLocalAuthorityName())
            .managingOrganisationName(managingOrganisation.getName())
            .build();
    }
}
