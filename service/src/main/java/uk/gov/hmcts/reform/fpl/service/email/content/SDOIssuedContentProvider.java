package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SDOIssuedContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper helper;

    public SDONotifyData buildNotificationParameters(CaseData caseData, DirectionsOrderType directionsOrderType) {
        return SDONotifyData.builder()
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .callout(buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .directionsOrderTypeShort(directionsOrderType.getShortForm())
            .directionsOrderTypeLong(directionsOrderType.getLongForm())
            .build();
    }

    public SDONotifyData buildNotificationParameters(CaseData caseData, DocumentReference order, String reason) {
        return SDONotifyData.builder()
            .courtName(caseData.getCourt().getName())
            .caseNumber(caseData.getFamilyManCaseNumber())
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .documentLink(linkToAttachedDocument(order))
            .isReasonPresent(Optional.ofNullable(reason).map(value -> "yes").orElse("no"))
            .reason(Optional.ofNullable(reason).orElse(""))
            .build();
    }
}
