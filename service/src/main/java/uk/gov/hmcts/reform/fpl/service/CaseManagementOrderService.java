package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;

//TODO: this class will take some of the methods out of draftCMO service. FPLA-1479
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final Time time;
    private final DraftCMOService draftCMOService;
    private final CaseManagementOrderGenerationService templateDataGenerationService;
    private final DocumentService documentService;

    public Map<String, Object> extractMapFieldsFromCaseManagementOrder(CaseManagementOrder order) {
        if (isNull(order)) {
            order = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put(SCHEDULE.getKey(), order.getSchedule());
        data.put(RECITALS.getKey(), order.getRecitals());
        data.put(ORDER_ACTION.getKey(), order.getAction());

        return data;
    }

    public CaseManagementOrder getOrder(CaseData caseData) {
        CaseManagementOrder preparedOrder = draftCMOService.prepareCaseManagementOrder(caseData);
        CaseData updatedCaseData = caseData.toBuilder().caseManagementOrder(preparedOrder).build();
        Document document = getDocument(updatedCaseData);

        preparedOrder.setOrderDocReferenceFromDocument(document);

        return preparedOrder;
    }

    public Document getDocument(CaseData caseData) {
        DocmosisCaseManagementOrder templateData = templateDataGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, CMO);
    }
}
