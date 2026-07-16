package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationRefusalOrder;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.REFUSAL_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationRefusalOrderService {
    private final CaseDataExtractionService dataService;
    private final Time time;

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService documentUploadService;
    private final DocumentSealingService  documentSealingService;

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
                                                                  String dateOfRefusal, String applicationDate,
                                                                  String refusalReason, boolean requireSealing) {
        DocmosisDocument docmosisDocument = generateApplicationRefusalOrderPDF(caseData,
            getTemplateData(caseData, judgeTitleAndName, dateOfRefusal, applicationDate, refusalReason));

        byte[] documentBytes = (requireSealing)
            ? documentSealingService.sealDocument(docmosisDocument.getBytes(), caseData.getCourt(), SealType.ENGLISH)
            : docmosisDocument.getBytes();

        return DocumentReference.buildFromDocument(
            documentUploadService.uploadPDF(documentBytes, getRefusalOrderTitle(applicationDate)));
    }

    public Element<GeneratedOrder> buildRefusalOrder(CaseData caseData, String judgeTitleAndName,
                                                     String applicationDate, String refusalReason,
                                                     boolean isConfidential) {
        return buildRefusalOrder(caseData, judgeTitleAndName,
            formatLocalDateToString(time.now().toLocalDate(), DATE, caseData.getCaseLanguage()),
            applicationDate, refusalReason, isConfidential);
    }

    public Element<GeneratedOrder> buildRefusalOrder(CaseData caseData, String judgeTitleAndName,
                                                     String dateOfRefusal, String applicationDate,
                                                     String refusalReason, boolean isConfidential) {

        DocumentReference refusalOrderDoc = buildApplicationRefusalOrderDocument(caseData, judgeTitleAndName,
            dateOfRefusal, applicationDate, refusalReason, true);

        GeneratedOrder.GeneratedOrderBuilder refusalOrderBuilder = GeneratedOrder.builder()
            .type(REFUSAL_ORDER.getLabel())
            .title(getRefusalOrderTitle(applicationDate))
            .dateOfIssue(dateOfRefusal)
            .judgeAndLegalAdvisor(null)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .children(caseData.getAllChildren());

        if (isConfidential) {
            refusalOrderBuilder = refusalOrderBuilder.documentConfidential(refusalOrderDoc);
        } else {
            refusalOrderBuilder = refusalOrderBuilder.document(refusalOrderDoc);
        }

        return element(refusalOrderBuilder.build());
    }

    private String getRefusalOrderTitle(String applicationDate) {
        return format("%s for application date %s", REFUSAL_ORDER.getLabel(), applicationDate);
    }
}
