package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicantsDetailsUpdatedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentFullName;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class ApplicantsDetailsUpdatedContentProvider extends AbstractEmailContentProvider {

    public ApplicantsDetailsUpdatedNotifyData getApplicantsDetailsUpdatedNotifyData(CaseData caseData) {
        String respondentName;

        if (caseData.getRepresentativeType() == RepresentativeType.LOCAL_AUTHORITY) {
            respondentName = getFirstRespondentLastName(caseData);
        } else {
            //Respondent is an LA
            respondentName = getFirstRespondentFullName(caseData.getAllRespondents());
        }

        return ApplicantsDetailsUpdatedNotifyData.builder()
            .firstRespondentLastName(respondentName)
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }
}
