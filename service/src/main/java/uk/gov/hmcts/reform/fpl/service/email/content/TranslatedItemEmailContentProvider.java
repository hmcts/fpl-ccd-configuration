package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.TranslatedItemNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TranslatedItemEmailContentProvider extends AbstractEmailContentProvider
    implements ModifiedItemEmailContentProvider {

    private final HmctsCourtLookupConfiguration config;
    private final EmailNotificationHelper helper;

    @Override
    public TranslatedItemNotifyData getNotifyData(final CaseData caseData, final DocumentReference orderDocument,
                                                  final String docType) {

        return TranslatedItemNotifyData.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .docType(docType)
            .courtName(config.getCourt(caseData.getCaseLocalAuthority()).getName())
            .callout("^" + buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }

}
