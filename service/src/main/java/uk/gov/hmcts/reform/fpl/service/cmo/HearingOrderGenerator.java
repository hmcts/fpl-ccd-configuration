package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisApprovedOrderCoverSheetService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingOrderGenerator {

    private final DocumentSealingService documentSealingService;
    private final Time time;
    private final DocmosisApprovedOrderCoverSheetService docmosisApprovedOrderCoverSheetService;
    private final DocumentMerger documentMerger;
    private final UploadDocumentService uploadDocumentService;

    public Element<HearingOrder> buildSealedHearingOrder(CaseData caseData,
                                                         ReviewDecision reviewDecision,
                                                         Element<HearingOrder> hearingOrderElement,
                                                         List<Element<Other>> selectedOthers,
                                                         String othersNotified,
                                                         boolean addCoverSheet) {
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

        DocumentReference sealedOrder = documentSealingService.sealDocument(order, caseData.getCourt(),
            caseData.getSealType());

        if (addCoverSheet) {
            try {
                DocmosisDocument orderWithCoverSheet = docmosisApprovedOrderCoverSheetService
                    .addCoverSheetToApprovedOrder(caseData, sealedOrder);

                sealedOrder = buildFromDocument(uploadDocumentService
                    .uploadPDF(orderWithCoverSheet.getBytes(), order.getFilename()));
            } catch (Exception e) {
                // TODO handle this better, maybe a notification to FPL service?
                log.error("Error adding cover sheet to order", e);
            }
        }

        builder = (isConfidentialOrder)
            ? builder.orderConfidential(sealedOrder)
            : builder.order(sealedOrder);

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
