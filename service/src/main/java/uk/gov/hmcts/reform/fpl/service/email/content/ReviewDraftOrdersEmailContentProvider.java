package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class ReviewDraftOrdersEmailContentProvider extends AbstractEmailContentProvider {

    public ApprovedOrdersTemplate buildOrdersApprovedNotificationParameters(CaseData caseData, CaseManagementOrder cmo,
                                                                       RepresentativeServingPreferences servingPreference) {
        return ApprovedOrdersTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .digitalPreference(hasDigitalServingPreference(servingPreference) ? "Yes" : "No")
            .documentLink((hasDigitalServingPreference(servingPreference)
                ? getDocumentUrl(cmo.getOrder()) : linkToAttachedDocument(cmo.getOrder())))
            .caseUrl((hasDigitalServingPreference(servingPreference) ? getCaseUrl(caseData.getId(), ORDERS) : ""))
            .build();
    }

    public RejectedOrdersTemplate buildOrdersRejectedNotificationParameters(final CaseData caseData,
                                                                                CaseManagementOrder cmo) {
        return RejectedOrdersTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .hearing(uncapitalize(cmo.getHearing()))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .requestedChanges(cmo.getRequestedChanges())
            .build();
    }

    private boolean hasDigitalServingPreference(RepresentativeServingPreferences servingPreference) {
        return servingPreference == DIGITAL_SERVICE;
    }
}
