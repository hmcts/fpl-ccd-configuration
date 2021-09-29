package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentGenerator;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGeneratorHolder;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;

@Service
@RequiredArgsConstructor
public class OrderNotificationDocumentService {

    private final DocumentGenerator documentGenerator;
    private final UploadDocumentService uploadDocumentService;
    private final OrderDocumentGeneratorHolder orderDocumentGeneratorHolder;

    public Optional<DocumentReference> createNotificationDocument(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        Order orderType = manageOrdersEventData.getManageOrdersType();

        return orderDocumentGeneratorHolder.getNotificationDocumentParameterGeneratorByOrderType(orderType)
            .map(generator -> documentGenerator.generateDocument(caseData, generator, PDF, SEALED))
            .map(generatedDoc ->
                uploadDocumentService.uploadPDF(generatedDoc.getBytes(), generatedDoc.getDocumentTitle()))
            .map(DocumentReference::buildFromDocument);
    }

}
