package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.action.AmendOrderAction;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {
    private static final String MEDIA_TYPE = RenderFormat.PDF.getMediaType();
    private static final String FILE_NAME_PREFIX = "amended_";

    private final AmendedOrderStamper stamper;
    private final List<AmendOrderAction> amendmentActions;
    private final UploadDocumentService uploadService;
    private final OthersService othersService;

    public Map<String, Object> updateOrder(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        AmendOrderAction amendmentAction = amendmentActions.stream()
            .filter(action -> action.accept(caseData))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to amend order for order with id \"%s\"",
                eventData.getManageOrdersAmendmentList().getValueCode()
            )));

        byte[] stampedBinaries = stamper.amendDocument(eventData.getManageOrdersAmendedOrder());
        String amendedFileName = updateFileName(eventData.getManageOrdersOrderToAmend());
        Document stampedDocument = uploadService.uploadDocument(stampedBinaries, amendedFileName, MEDIA_TYPE);

        return amendmentAction.applyAmendedOrder(caseData, buildFromDocument(stampedDocument));
    }

    private String updateFileName(DocumentReference original) {
        String filename = original.getFilename();
        return filename.startsWith(FILE_NAME_PREFIX) ? filename : FILE_NAME_PREFIX + filename;
    }
}
