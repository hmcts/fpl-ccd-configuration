package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadedOrderDocumentGenerator {

    private final DocumentSealingService documentSealingService;
    private final DocumentConversionService documentConversionService;
    private final DocumentDownloadService documentDownloadService;

    public OrderDocumentGeneratorResult generate(CaseData caseData, OrderStatus status, RenderFormat format) {

        DocumentReference documentReference = caseData.getManageOrdersEventData().getManageOrdersUploadOrderFile();
        byte[] bytes = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());

        if (RenderFormat.PDF == format) {
            bytes = documentConversionService.convertToPdf(bytes, documentReference.getFilename());

            if (needSealing(caseData, status)) {
                bytes = documentSealingService.sealDocument(bytes, SealType.ENGLISH);
            }

            return new OrderDocumentGeneratorResult(bytes, RenderFormat.PDF);
        }

        return new OrderDocumentGeneratorResult(bytes, RenderFormat.fromFileName(documentReference.getFilename()));
    }

    private boolean needSealing(CaseData caseData, OrderStatus status) {
        return status == OrderStatus.SEALED
            && YesNo.YES == YesNo.fromString(caseData.getManageOrdersEventData().getManageOrdersNeedSealing());
    }

}
