package uk.gov.hmcts.reform.fpl.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StandardDirectionsOrderService {
    private final DocumentConversionService conversionService;
    private final DocumentSealingService sealingService;
    private final Time time;
    private final IdamClient idamClient;
    private final RequestData requestData;

    public StandardDirectionOrder buildOrderFromUpload(StandardDirectionOrder currentOrder) throws Exception {
        UserInfo userInfo = idamClient.getUserInfo(requestData.authorisation());

        return StandardDirectionOrder.builder()
            .orderStatus(currentOrder.getOrderStatus())
            .dateOfUpload(time.now().toLocalDate())
            .uploader(userInfo.getName())
            .orderDoc(prepareOrderDocument(currentOrder.getOrderDoc(), currentOrder.getOrderStatus()))
            .build();
    }

    private DocumentReference prepareOrderDocument(DocumentReference document, OrderStatus status) throws Exception {
        if (status != OrderStatus.SEALED) {
            return document;
        }
        DocumentReference pdf = conversionService.convertToPdf(document);
        return sealingService.sealDocument(pdf);
    }
}
