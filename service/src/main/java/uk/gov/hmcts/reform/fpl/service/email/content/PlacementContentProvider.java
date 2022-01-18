package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;

@Component
@RequiredArgsConstructor
public class PlacementContentProvider extends AbstractEmailContentProvider {

    public PlacementNotifyData getApplicationChangedCourtData(CaseData caseData, Placement placement) {

        return PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .caseUrl(getCaseUrl(caseData.getId(), PLACEMENT))
            .build();
    }

    public PlacementNotifyData getNoticeChangedData(CaseData caseData, Placement placement) {

        return PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .caseUrl(getCaseUrl(caseData.getId(), PLACEMENT))
            .build();
    }

    public PlacementNotifyData getNoticeChangedCafcassData(CaseData caseData, Placement placement) {
        final Object documentDownloadUrl = getDocumentDownloadLink(placement.getPlacementNotice());

        return PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .ccdNumber(caseData.getId().toString())
            .documentUrl(getDocumentUrl(placement.getPlacementNotice()))
            .documentDownloadUrl(documentDownloadUrl)
            .hasDocumentDownloadUrl(isEmpty(documentDownloadUrl) ? "no" : "yes")
            .build();
    }

    private Object getDocumentDownloadLink(DocumentReference document) {
        try {
            return linkToAttachedDocument(document);
        } catch (RuntimeException e) {
            return EMPTY;
        }
    }
}
