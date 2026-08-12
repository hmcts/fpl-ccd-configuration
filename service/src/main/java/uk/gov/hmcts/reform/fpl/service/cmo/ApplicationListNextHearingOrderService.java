package uk.gov.hmcts.reform.fpl.service.cmo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicationListNextHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.orders.AbstractApplicationGeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPLICATION_LIST_NEXT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.LIST_AT_NEXT_HEARING_ORDER;

@Service
public class ApplicationListNextHearingOrderService extends AbstractApplicationGeneratedOrderService {

    @Autowired
    public ApplicationListNextHearingOrderService(CaseDataExtractionService dataService,
                                                  Time time,
                                                  DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                                  UploadDocumentService documentUploadService,
                                                  DocumentSealingService documentSealingService) {
        super(dataService, time, docmosisDocumentGeneratorService, documentUploadService, documentSealingService);
    }

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

    public Element<GeneratedOrder> buildListAtNextHearingOrder(CaseData caseData,
                                                                String judgeTitleAndName,
                                                                String applicationDate,
                                                                String nextHearingDate,
                                                                boolean isConfidential) {
        return buildListAtNextHearingOrderWithDateOfIssue(
            caseData,
            judgeTitleAndName,
            getDateOfIssue(caseData),
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
        return buildGeneratedOrder(
            caseData,
            LIST_AT_NEXT_HEARING_ORDER.getLabel(),
            getOrderTitle(applicationDate),
            dateOfIssue,
            orderDoc,
            isConfidential
        );
    }

    private String getOrderTitle(String applicationDate) {
        return format("%s for application date %s", LIST_AT_NEXT_HEARING_ORDER.getLabel(),
            getApplicationDateOnly(applicationDate));
    }

    private String getApplicationDateOnly(String applicationDate) {
        if (applicationDate == null) {
            return null;
        }

        String[] dateAndTime = applicationDate.split(",", 2);
        return dateAndTime[0].trim();
    }

    private String getOrderFileName(String applicationDate) {
        return getOrderTitle(applicationDate) + ".pdf";
    }
}



