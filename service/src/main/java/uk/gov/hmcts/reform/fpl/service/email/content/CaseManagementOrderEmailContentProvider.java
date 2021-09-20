package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseManagementOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public IssuedCMOTemplate buildCMOIssuedNotificationParameters(CaseData caseData, HearingOrder cmo,
                                                                  RepresentativeServingPreferences servingPreference) {
        return IssuedCMOTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .digitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No")
            .documentLink((hasDigitalServingPreference(servingPreference)
                ? getDocumentUrl(cmo.getOrder()) : linkToAttachedDocument(cmo.getOrder())))
            .caseUrl((hasDigitalServingPreference(servingPreference) ? getCaseUrl(caseData.getId(), ORDERS) : ""))
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }

    public RejectedCMOTemplate buildCMORejectedByJudgeNotificationParameters(final CaseData caseData,
                                                                             HearingOrder cmo) {
        return RejectedCMOTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .requestedChanges(cmo.getRequestedChanges())
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
