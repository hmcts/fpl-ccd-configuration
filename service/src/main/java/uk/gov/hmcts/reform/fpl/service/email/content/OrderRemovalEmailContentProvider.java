package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalEmailContentProvider extends AbstractEmailContentProvider {

    public OrderRemovalTemplate buildNotificationForOrderRemoval(CaseData caseData, String removalReason) {
        return OrderRemovalTemplate.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseUrl(getCaseUrl(caseData.getId()))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .removalReason(removalReason)
            .build();
    }

}
