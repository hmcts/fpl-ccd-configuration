package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrderPreviewSectionPrePopulator implements OrderSectionPrePopulator {

    private static final String DRAFT_ORDER_NAME = "Preview order.pdf (opens in a new tab)";
    private static final String ORDER_PREVIEW_FIELD = "orderPreview";

    private final OrderDocumentGenerator orderDocumentGenerator;
    private final UploadDocumentService uploadService;

    @Override
    public OrderSection accept() {
        return OrderSection.REVIEW;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        DocmosisDocument draftOrder = orderDocumentGenerator.generate(
            caseData.getManageOrdersEventData().getManageOrdersType(), caseData, DRAFT
        );

        Document document = uploadService.uploadPDF(draftOrder.getBytes(), DRAFT_ORDER_NAME);
        DocumentReference orderPreview = DocumentReference.buildFromDocument(document);

        return Map.of(ORDER_PREVIEW_FIELD, orderPreview);
    }
}
