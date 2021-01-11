package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabLabel.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationContentProvider extends AbstractEmailContentProvider {

    public BaseCaseNotifyData buildPlacementApplicationNotificationParameters(CaseData caseData) {

        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), PLACEMENT))
            .respondentLastName(getFirstRespondentLastName(caseData))
            .build();
    }
}
