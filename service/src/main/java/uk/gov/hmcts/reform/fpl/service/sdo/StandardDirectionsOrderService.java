package uk.gov.hmcts.reform.fpl.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class StandardDirectionsOrderService {
    private final DocumentSealingService sealingService;
    private final Time time;
    private final IdamClient idamClient;
    private final RequestData requestData;

    public LocalDate generateDateOfIssue(StandardDirectionOrder order) {
        LocalDate dateOfIssue = time.now().toLocalDate();

        if (order != null && order.getDateOfIssue() != null) {
            dateOfIssue = parseLocalDateFromStringUsingFormat(order.getDateOfIssue(), DATE);
        }

        return dateOfIssue;
    }

    public StandardDirectionOrder buildTemporarySDO(CaseData caseData, StandardDirectionOrder previousSDO) {
        DocumentReference document = caseData.getPreparedSDO();

        if (document == null) {
            // been through once, either pull from replacement doc or SDO if that isn't present
            document = defaultIfNull(
                caseData.getReplacementSDO(),
                previousSDO.getOrderDoc()
            );
        }
        return StandardDirectionOrder.builder()
            .orderDoc(document)
            .build();
    }

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
        return sealingService.sealDocument(document);
    }
}
