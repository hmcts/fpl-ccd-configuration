package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSourceType;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGeneratorResult;
import uk.gov.hmcts.reform.fpl.service.orders.generator.UploadedOrderDocumentGenerator;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderCreationService {
    private static final String DRAFT_ORDER_NAME = "Preview order.pdf";

    private final OrderDocumentGenerator orderGenerator;
    private final UploadedOrderDocumentGenerator uploadedOrderGenerator;
    private final UploadDocumentService uploadService;

    public DocumentReference createOrderDocument(CaseData caseData, OrderStatus status, RenderFormat format) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order orderType = manageOrdersEventData.getManageOrdersType();

        OrderDocumentGeneratorResult result = generateContent(caseData, status, format, orderType);

        String orderName = status == DRAFT ? DRAFT_ORDER_NAME :
            orderType.fileName(result.getRenderFormat(), manageOrdersEventData);

        Document document = uploadService.uploadDocument(
            result.getBytes(),
            orderName,
            result.getRenderFormat().getMediaType()
        );

        return DocumentReference.buildFromDocument(document);
    }

    private OrderDocumentGeneratorResult generateContent(CaseData caseData, OrderStatus status, RenderFormat format,
                                                         Order orderType) {

        if (OrderSourceType.MANUAL_UPLOAD == orderType.getSourceType()) {
            return uploadedOrderGenerator.generate(caseData, status, format);
        }

        return OrderDocumentGeneratorResult.builder()
            .bytes(orderGenerator.generate(orderType, caseData, status, format).getBytes())
            .renderFormat(format)
            .build();
    }
}
