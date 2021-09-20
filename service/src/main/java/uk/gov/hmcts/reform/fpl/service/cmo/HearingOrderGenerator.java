package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingOrderGenerator {

    private final DocumentSealingService documentSealingService;
    private final Time time;

    public Element<HearingOrder> buildSealedHearingOrder(ReviewDecision reviewDecision,
                                                         Element<HearingOrder> hearingOrderElement,
                                                         List<Element<Other>> selectedOthers,
                                                         String othersNotified) {
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = hearingOrderElement.getValue().getOrder();
        }

        return element(hearingOrderElement.getId(), hearingOrderElement.getValue().toBuilder()
            .dateIssued(time.now().toLocalDate())
            .status(CMOStatus.APPROVED)
            .order(documentSealingService.sealDocument(order))
            .lastUploadedOrder(order)
            .others(selectedOthers)
            .othersNotified(othersNotified)
            .build());
    }

    public Element<HearingOrder> buildRejectedHearingOrder(
        Element<HearingOrder> hearingOrderElement, String changesRequested) {
        return element(hearingOrderElement.getId(), hearingOrderElement.getValue().toBuilder()
            .status(CMOStatus.RETURNED)
            .requestedChanges(changesRequested)
            .build());
    }
}
