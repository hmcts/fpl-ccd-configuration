package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

//TODO: this class will take some of the methods out of draftCMO service. FPLA-1479
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final Time time;
    private final DraftCMOService draftCMOService;
    private final CaseManagementOrderGenerationService templateDataGenerationService;
    private final DocumentService documentService;

    public LocalDate getIssuedDate(CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDateOfIssue())) {
            return time.now().toLocalDate();
        }

        return parseLocalDateFromStringUsingFormat(caseManagementOrder.getDateOfIssue(), DATE);
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
