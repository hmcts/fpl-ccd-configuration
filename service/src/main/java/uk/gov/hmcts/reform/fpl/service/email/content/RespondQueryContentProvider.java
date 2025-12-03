package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RespondQueryNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
public class RespondQueryContentProvider extends AbstractEmailContentProvider {

    public RespondQueryNotifyData getRespondQueryNotifyData(CaseData caseData, String queryDate) {
        return RespondQueryNotifyData.builder()
            .caseId(defaultIfNull(caseData.getId(), "").toString())
            .caseName(defaultIfNull(caseData.getCaseName(), ""))
            .caseUrl(defaultIfNull(getCaseUrl(caseData.getId()), ""))
            .queryDate(queryDate)
            .build();
    }
}
