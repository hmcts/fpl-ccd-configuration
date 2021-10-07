package uk.gov.hmcts.reform.fpl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;

import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;

@Slf4j
@Service
public class PlacementNoticeEventHandler {

    @Async
    @EventListener
    public void takePayment(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();

        final PlacementNoticeDocument notice = event.getNotice();
        final PlacementNoticeDocument.RecipientType type = notice.getType();

        if (type == LOCAL_AUTHORITY) {
            notifyLocalAuthority(caseData, notice);
        }
        if (type == CAFCASS) {
            notifyCafcass(caseData, notice);
        }
        if (type == PARENT_FIRST || type == PARENT_SECOND) {
            notifyParent(caseData, notice);
        }

    }

    private void notifyLocalAuthority(CaseData caseData, PlacementNoticeDocument notice) {
        log.info("To be implemented by DFPL-112");
    }

    private void notifyCafcass(CaseData caseData, PlacementNoticeDocument notice) {
        log.info("To be implemented by DFPL-112");
    }

    private void notifyParent(CaseData caseData, PlacementNoticeDocument notice) {
        log.info("To be implemented by DFPL-112");
    }

}
