package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.courtbundle.CourtBundleUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtBundleUploadedEmailContentProvider extends AbstractEmailContentProvider {

    public CourtBundleUploadedData buildParameters(CaseData caseData, String hearingDetails) {
        return CourtBundleUploadedData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), COURT_BUNDLE))
            .hearingDetails(hearingDetails)
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }
}
