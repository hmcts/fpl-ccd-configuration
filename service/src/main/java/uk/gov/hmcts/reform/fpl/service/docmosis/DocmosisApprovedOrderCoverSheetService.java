package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApprovedOrderCoverSheet;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPROVED_ORDER_COVER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisApprovedOrderCoverSheetService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DocumentMerger documentMerger;
    private final Time time;

    public DocmosisDocument addCoverSheetToApprovedOrder(CaseData caseData,
                                                         DocumentReference order) throws IOException {
        // Create
        DocmosisDocument coverSheet = createCoverSheet(caseData);

        // TODO could we use the byte array from the approved order instead of downloading it again?
        // Add cover sheet to the order
        DocmosisDocument orderWithCoverSheet = documentMerger.mergeDocuments(coverSheet, List.of(order));

        // Add the ANNEX keyword to the second page top of the merged file
        try (PDDocument document = PDDocument.load(orderWithCoverSheet.getBytes())) {
            if (document.getNumberOfPages() < 2) {
                throw new IllegalArgumentException("The order cover sheet must have at least 2 pages.");
            }

            PDPage secondPage = document.getPage(1); // Index 1 is the second page
            PDRectangle pageSize = secondPage.getMediaBox();
            try (PDPageContentStream contentStream = new PDPageContentStream(document, secondPage,
                PDPageContentStream.AppendMode.APPEND, true, true)) {

                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_BOLD, 12);

                float textX = 50;
                float textY = pageSize.getHeight() - 45;
                contentStream.newLineAtOffset(textX,  textY); // Adjust position
                contentStream.showText("ANNEX A:");
                contentStream.endText();

                // Calculate underline position and length
                float textWidth = PDType1Font.TIMES_BOLD.getStringWidth("ANNEX A:") / 1000 * 12; // Font size is 12
                float underlineY = textY - 2; // Slightly below the text

                // Draw underline
                contentStream.moveTo(textX, underlineY);
                contentStream.lineTo(textX + textWidth, underlineY);
                contentStream.setLineWidth(0.5f); // Adjust thickness
                contentStream.stroke();
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream);
                return new DocmosisDocument(orderWithCoverSheet.getDocumentTitle(), outputStream.toByteArray());
            }
        }
    }

    public DocmosisDocument createCoverSheet(CaseData caseData) {
        DocmosisApprovedOrderCoverSheet coverDocumentData = buildCoverDocumentsData(caseData);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(coverDocumentData,
            APPROVED_ORDER_COVER,
            RenderFormat.PDF,
            Language.ENGLISH);
    }

    public DocmosisApprovedOrderCoverSheet buildCoverDocumentsData(CaseData caseData) {
        return DocmosisApprovedOrderCoverSheet.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(caseDataExtractionService.getCourtName(caseData))
            .children(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren()))
            .judgeTitleAndName(caseData.getReviewDraftOrdersData().getJudgeTitleAndName())
            .dateOfApproval(formatLocalDateToString(time.now().toLocalDate(), DATE, Language.ENGLISH))
            .crest(CREST.getValue())
            .build();
    }
}
