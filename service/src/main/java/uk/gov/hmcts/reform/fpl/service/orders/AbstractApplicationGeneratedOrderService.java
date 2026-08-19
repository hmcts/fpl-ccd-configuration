package uk.gov.hmcts.reform.fpl.service.orders;

import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.function.Consumer;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public abstract class AbstractApplicationGeneratedOrderService {

    protected final CaseDataExtractionService dataService;
    protected final Time time;
    protected final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    protected final UploadDocumentService documentUploadService;
    protected final DocumentSealingService documentSealingService;

    protected AbstractApplicationGeneratedOrderService(CaseDataExtractionService dataService,
                                                       Time time,
                                                       DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                                       UploadDocumentService documentUploadService,
                                                       DocumentSealingService documentSealingService) {
        this.dataService = dataService;
        this.time = time;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.documentUploadService = documentUploadService;
        this.documentSealingService = documentSealingService;
    }

    protected String getDateOfIssue(CaseData caseData) {
        return formatLocalDateToString(time.now().toLocalDate(), DATE, caseData.getCaseLanguage());
    }

    protected DocumentReference buildOrderDocumentReference(CaseData caseData,
                                                            boolean requireSealing,
                                                            DocmosisDocument docmosisDocument,
                                                            String fileName) {
        byte[] documentBytes = requireSealing
            ? documentSealingService.sealDocument(docmosisDocument.getBytes(), caseData.getCourt(), SealType.ENGLISH)
            : docmosisDocument.getBytes();

        return DocumentReference.buildFromDocument(documentUploadService.uploadPDF(documentBytes, fileName));
    }

    protected DocmosisDocument generateApplicationOrderPDF(CaseData caseData, DocmosisTemplates template,
                                                           DocmosisData templateData) {
        return docmosisDocumentGeneratorService.generateDocmosisDocument(templateData,
            template);

    }

    protected Element<GeneratedOrder> buildGeneratedOrder(CaseData caseData,
                                                          String generatedOrderType,
                                                          String title,
                                                          String dateOfIssue,
                                                          DocumentReference orderDoc,
                                                          boolean isConfidential) {
        return buildGeneratedOrder(
            caseData,
            generatedOrderType,
            title,
            dateOfIssue,
            orderDoc,
            builder -> {
                if (isConfidential) {
                    builder.documentConfidential(orderDoc);
                } else {
                    builder.document(orderDoc);
                }
            }
        );
    }

    protected Element<GeneratedOrder> buildGeneratedOrder(CaseData caseData,
                                                          String generatedOrderType,
                                                          String title,
                                                          String dateOfIssue,
                                                          DocumentReference orderDoc,
                                                          Consumer<GeneratedOrder.GeneratedOrderBuilder> documentBinder) {
        GeneratedOrder.GeneratedOrderBuilder generatedOrderBuilder = GeneratedOrder.builder()
            .type(generatedOrderType)
            .title(title)
            .dateOfIssue(dateOfIssue)
            .judgeAndLegalAdvisor(null)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .children(caseData.getAllChildren());

        documentBinder.accept(generatedOrderBuilder);

        return element(generatedOrderBuilder.build());
    }


    protected String buildApplicationOrderTitle(String orderTypeLabel, String applicationDate) {
        return format("%s for application date %s", orderTypeLabel, applicationDate);
    }

    protected String buildApplicationOrderFileName(String orderTypeLabel, String applicationDate) {
        return buildApplicationOrderTitle(orderTypeLabel, applicationDate) + ".pdf";
    }
}

