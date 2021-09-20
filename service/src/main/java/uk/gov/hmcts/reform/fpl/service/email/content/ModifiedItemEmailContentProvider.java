package uk.gov.hmcts.reform.fpl.service.email.content;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.ModifiedItemNotifyData;

public interface ModifiedItemEmailContentProvider {

    ModifiedItemNotifyData getNotifyData(final CaseData caseData,
                                         final DocumentReference orderDocument,
                                         final String orderType);

}
