package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderCreationService {
    private static final String DRAFT_ORDER_NAME = "Preview order.pdf";

    private final OrderDocumentGenerator generator;
    private final UploadDocumentService uploadService;

    public DocumentReference createOrderDocument(CaseData caseData, OrderStatus status, RenderFormat format) {
        Order orderType = caseData.getManageOrdersEventData().getManageOrdersType();

        DocmosisDocument docmosisDocument = generator.generate(orderType, caseData, status, format);

        String orderName = status == DRAFT ? DRAFT_ORDER_NAME : orderType.fileName(format);

        Document document = uploadService.uploadDocument(docmosisDocument.getBytes(), orderName, format.getMediaType());

        return DocumentReference.buildFromDocument(document);
    }
}
