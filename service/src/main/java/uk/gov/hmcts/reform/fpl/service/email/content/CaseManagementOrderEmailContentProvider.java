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

        IssuedCMOTemplate template = new IssuedCMOTemplate();

        template.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        template.setHearing(uncapitalize(cmo.getHearing()));
        template.setDigitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No");
        template.setDocumentLink(linkToAttachedDocument(cmo.getOrder()));
        template.setCaseUrl((hasDigitalServingPreference(servingPreference)
            ? getCaseUrl(caseData.getId(), "OrdersTab") : ""));

        return template;
    }

    public RejectedCMOTemplate buildCMORejectedByJudgeNotificationParameters(final CaseData caseData,
                                                                             CaseManagementOrder cmo) {
        RejectedCMOTemplate template = new RejectedCMOTemplate();

        template.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        template.setHearing(uncapitalize(cmo.getHearing()));
        template.setCaseUrl(getCaseUrl(caseData.getId(), "OrdersTab"));
        template.setRequestedChanges(cmo.getRequestedChanges());

        return template;
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
