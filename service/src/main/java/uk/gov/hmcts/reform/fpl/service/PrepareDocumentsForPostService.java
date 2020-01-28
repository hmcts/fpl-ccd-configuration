package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.GENERAL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

public class PrepareDocumentsForPostService {
    private final RepresentativeService representativeService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final RequestData requestData;

    public PrepareDocumentsForPostService(RepresentativeService representativeService,
                                          DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                          UploadDocumentService uploadDocumentService,
                                          RequestData requestData) {
        this.representativeService = representativeService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.requestData = requestData;
    }

    public Document getPostDocumentsAsSinglePdf(CaseData caseData, Document mainDocument) {
        Document combinedPdf = new Document();
        List<Representative> representativesServedByPost =
            representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), POST);

        for (Representative representative : representativesServedByPost) {
            createGeneralLetter(caseData, representative);
            createCoverSheet(caseData, representative);
            //CALL STITCHING SERVICE
            //upload bundle etc
        }
        return combinedPdf;
    }

    private Document createGeneralLetter(CaseData caseData, Representative representative) {
        Document generalLetter = getDocument(caseData, representative, GENERAL);

        return generalLetter;
    }

    private Document createCoverSheet(CaseData caseData, Representative representative) {
        Document coverSheet = new Document();
        return coverSheet;
    }

    private Map<String, Object> getTemplateData(CaseData caseData, Representative representative) {
        ImmutableMap.Builder<String, Object> templateBuilder = new ImmutableMap.Builder<>();
        //REPRESENTATIVE NAME/ADDRESS
        //COURT ADDRESS
        //FAMILY MAN CASE NUMBER (FOR GENERAL LETTER)
        //CCD REFERENCE NUMBER (FOR COVER SHEET)
        return templateBuilder.build();
    }

    private Document getDocument(CaseData caseData, Representative representative, DocmosisTemplates template) {

        DocmosisDocument document = docmosisDocumentGeneratorService.generateDocmosisDocument(getTemplateData(caseData,
            representative), template);

        return uploadDocumentService.uploadPDF(requestData.userId(), requestData.authorisation(), document.getBytes(),
            GENERAL.getDocumentTitle());
    }
}
