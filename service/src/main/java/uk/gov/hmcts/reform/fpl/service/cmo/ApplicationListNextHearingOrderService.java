package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationListNextHearingOrder;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPLICATION_LIST_NEXT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.LIST_AT_NEXT_HEARING_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationListNextHearingOrderService {

    private final CaseDataExtractionService dataService;
    private final Time time;

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService documentUploadService;
    private final DocumentSealingService documentSealingService;

    private DocmosisApplicationListNextHearingOrder getTemplateData(CaseData caseData,
                                                                    String judgeTitleAndName,
                                                                    String dateOfIssue,
                                                                    String applicationDate,
                                                                    String nextHearingDate) {
        return DocmosisApplicationListNextHearingOrder.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(dataService.getCourtName(caseData))
            .children(dataService.getChildrenDetails(caseData.getAllChildren()))
            .judgeTitleAndName(judgeTitleAndName)
            .dateOfIssue(dateOfIssue)
            .crest(CREST.getValue())
            .applicationDate(applicationDate)
            .nextHearingDate(nextHearingDate)
            .build();
    }

    public DocumentReference buildApplicationListedAtNextHearingOrderDocument(CaseData caseData,
                                                                               String judgeTitleAndName,
                                                                               String dateOfIssue,
                                                                               String applicationDate,
                                                                               String nextHearingDate,
                                                                               boolean requireSealing) {
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData, judgeTitleAndName, dateOfIssue, applicationDate, nextHearingDate),
            APPLICATION_LIST_NEXT_HEARING
        );

        return buildOrderDocumentReference(caseData, requireSealing, docmosisDocument,
            getOrderFileName(applicationDate));
    }

    private DocumentReference buildOrderDocumentReference(CaseData caseData,
                                                          boolean requireSealing,
                                                          DocmosisDocument docmosisDocument,
                                                          String fileName) {

        byte[] documentBytes = requireSealing
            ? documentSealingService.sealDocument(docmosisDocument.getBytes(), caseData.getCourt(), SealType.ENGLISH)
            : docmosisDocument.getBytes();

        return DocumentReference.buildFromDocument(documentUploadService.uploadPDF(documentBytes, fileName));
    }

    public Element<GeneratedOrder> buildListAtNextHearingOrder(CaseData caseData,
                                                                String judgeTitleAndName,
                                                                String applicationDate,
                                                                String nextHearingDate,
                                                                boolean isConfidential) {
        return buildListAtNextHearingOrderWithDateOfIssue(
            caseData,
            judgeTitleAndName,
            formatLocalDateToString(time.now().toLocalDate(), DATE, caseData.getCaseLanguage()),
            applicationDate,
            nextHearingDate,
            isConfidential
        );
    }

    private Element<GeneratedOrder> buildListAtNextHearingOrderWithDateOfIssue(CaseData caseData,
                                                                        String judgeTitleAndName,
                                                                        String dateOfIssue,
                                                                        String applicationDate,
                                                                        String nextHearingDate,
                                                                        boolean isConfidential) {
        DocumentReference listedOrderDoc = buildApplicationListedAtNextHearingOrderDocument(
            caseData,
            judgeTitleAndName,
            dateOfIssue,
            applicationDate,
            nextHearingDate,
            true
        );

        return buildGeneratedOrder(caseData, dateOfIssue, applicationDate, listedOrderDoc, isConfidential);
    }

    private Element<GeneratedOrder> buildGeneratedOrder(CaseData caseData,
                                                         String dateOfIssue,
                                                         String applicationDate,
                                                         DocumentReference orderDoc,
                                                         boolean isConfidential) {

        GeneratedOrder.GeneratedOrderBuilder generatedOrderBuilder = GeneratedOrder.builder()
            .type(LIST_AT_NEXT_HEARING_ORDER.getLabel())
            .title(getOrderTitle(applicationDate))
            .dateOfIssue(dateOfIssue)
            .judgeAndLegalAdvisor(null)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .children(caseData.getAllChildren());

        if (isConfidential) {
            generatedOrderBuilder = generatedOrderBuilder.documentConfidential(orderDoc);
        } else {
            generatedOrderBuilder = generatedOrderBuilder.document(orderDoc);
        }

        return element(generatedOrderBuilder.build());
    }

    private String getOrderTitle(String applicationDate) {
        return format("%s for application date %s", LIST_AT_NEXT_HEARING_ORDER.getLabel(), applicationDate);
    }

    private String getOrderFileName(String applicationDate) {
        return getOrderTitle(applicationDate) + ".pdf";
    }
}



