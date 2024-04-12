package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
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
                                                         String othersNotified,
                                                         SealType sealType,
                                                         Court court) {
        DocumentReference order;

        boolean isConfidentialOrder = hearingOrderElement.getValue().isConfidentialOrder();

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = (isConfidentialOrder) ? hearingOrderElement.getValue().getOrderConfidential()
                : hearingOrderElement.getValue().getOrder();
        }

        HearingOrder.HearingOrderBuilder builder = hearingOrderElement.getValue().toBuilder()
            .dateIssued(time.now().toLocalDate())
            .status(CMOStatus.APPROVED)
            .lastUploadedOrder(order)
            .others(selectedOthers)
            .othersNotified(othersNotified);

        builder = (isConfidentialOrder)
            ? builder.orderConfidential(documentSealingService.sealDocument(order, court, sealType))
            : builder.order(documentSealingService.sealDocument(order, court, sealType));

        return element(hearingOrderElement.getId(), builder.build());
    }

    public Element<HearingOrder> buildRejectedHearingOrder(
        Element<HearingOrder> hearingOrderElement, String changesRequested) {
        return element(hearingOrderElement.getId(), hearingOrderElement.getValue().toBuilder()
            .status(CMOStatus.RETURNED)
            .requestedChanges(changesRequested)
            .build());
    }
}
