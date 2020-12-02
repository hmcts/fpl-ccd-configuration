package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {

    public IssuedCMOTemplate buildCMOIssuedNotificationParameters(CaseData caseData, CaseManagementOrder cmo,
                                                                  RepresentativeServingPreferences servingPreference) {
        return IssuedCMOTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .digitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No")
            .documentLink(linkToAttachedDocument(cmo.getOrder()))
            .caseUrl((hasDigitalServingPreference(servingPreference)
                ? getCaseUrl(caseData.getId(), "OrdersTab") : ""))
            .build();
    }

    public RejectedCMOTemplate buildCMORejectedByJudgeNotificationParameters(final CaseData caseData,
                                                                             CaseManagementOrder cmo) {
        return RejectedCMOTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .caseUrl(getCaseUrl(caseData.getId(), "OrdersTab"))
            .requestedChanges(cmo.getRequestedChanges())
            .build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
