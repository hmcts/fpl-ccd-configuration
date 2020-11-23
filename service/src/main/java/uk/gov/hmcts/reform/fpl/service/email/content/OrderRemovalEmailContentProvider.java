package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderRemovalEmailContentProvider extends AbstractEmailContentProvider {

    public OrderRemovalTemplate buildNotificationForOrderRemoval(CaseData caseData, String removalReason) {
        OrderRemovalTemplate template = new OrderRemovalTemplate();
        template.setCaseReference(String.valueOf(caseData.getId()));
        template.setCaseUrl(getCaseUrl(caseData.getId()));
        template.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        template.setReturnedNote(removalReason);

        return template;
    }

}
