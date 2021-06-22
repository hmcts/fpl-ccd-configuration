package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PlacementApplicationContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public BaseCaseNotifyData buildPlacementApplicationNotificationParameters(CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .caseUrl(getCaseUrl(caseData.getId(), PLACEMENT))
            .lastName(helper.getSubjectLineLastName(caseData))
            .build();
    }
}
