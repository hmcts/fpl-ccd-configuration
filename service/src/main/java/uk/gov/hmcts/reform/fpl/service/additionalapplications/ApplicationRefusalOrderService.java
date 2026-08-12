package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationRefusalOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.AbstractApplicationGeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.REFUSAL_ORDER;

@Service
public class ApplicationRefusalOrderService extends AbstractApplicationGeneratedOrderService {

    @Autowired
    public ApplicationRefusalOrderService(CaseDataExtractionService dataService,
                                          Time time,
                                          DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                          UploadDocumentService documentUploadService,
                                          DocumentSealingService documentSealingService) {
        super(dataService, time, docmosisDocumentGeneratorService, documentUploadService, documentSealingService);
    }

    private DocmosisApplicationRefusalOrder getTemplateData(CaseData caseData, String judgeTitleAndName,
                                                            String dateOfRefusal, String applicationDate,
                                                            String refusalReason) {
        return DocmosisApplicationRefusalOrder.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(dataService.getCourtName(caseData))
            .children(dataService.getChildrenDetails(caseData.getAllChildren()))
            .judgeTitleAndName(judgeTitleAndName)
            .dateOfRefusal(dateOfRefusal)
            .crest(CREST.getValue())
            .applicationDate(applicationDate)
            .refusalReason(refusalReason)
            .build();
    }

    private DocmosisDocument generateApplicationRefusalOrderPDF(CaseData caseData,
                                                                DocmosisApplicationRefusalOrder templateData) {
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData,
            DocmosisTemplates.APPLICATION_REFUSAL_ORDER);
    }

    public DocumentReference buildApplicationRefusalOrderDocument(CaseData caseData, String judgeTitleAndName,
                                                                  String applicationDate,
                                                                  String refusalReason, boolean requireSealing) {
        return buildApplicationRefusalOrderDocument(caseData, judgeTitleAndName,
            getDateOfIssue(caseData), applicationDate,
            refusalReason, requireSealing);
    }

    public DocumentReference buildApplicationRefusalOrderDocument(CaseData caseData, String judgeTitleAndName,
                                                                  String dateOfRefusal, String applicationDate,
                                                                  String refusalReason, boolean requireSealing) {
        DocmosisDocument docmosisDocument = generateApplicationRefusalOrderPDF(caseData,
            getTemplateData(caseData, judgeTitleAndName, dateOfRefusal, applicationDate, refusalReason));

        return buildOrderDocumentReference(caseData, requireSealing, docmosisDocument, REFUSAL_ORDER.getFileName());
    }

    public Element<GeneratedOrder> buildRefusalOrder(CaseData caseData, String judgeTitleAndName,
                                                     String applicationDate, String refusalReason,
                                                     boolean isConfidential) {
        return buildRefusalOrder(caseData, judgeTitleAndName,
            getDateOfIssue(caseData),
            applicationDate, refusalReason, isConfidential);
    }

    public Element<GeneratedOrder> buildRefusalOrder(CaseData caseData, String judgeTitleAndName,
                                                     String dateOfRefusal, String applicationDate,
                                                     String refusalReason, boolean isConfidential) {

        DocumentReference refusalOrderDoc = buildApplicationRefusalOrderDocument(caseData, judgeTitleAndName,
            dateOfRefusal, applicationDate, refusalReason, true);

        return buildGeneratedOrder(
            caseData,
            REFUSAL_ORDER.getLabel(),
            getRefusalOrderTitle(applicationDate),
            dateOfRefusal,
            refusalOrderDoc,
            isConfidential
        );
    }

    public String getRefusalOrderTitle(String applicationDate) {
        return format("%s for application date %s", REFUSAL_ORDER.getLabel(), applicationDate);
    }
}

