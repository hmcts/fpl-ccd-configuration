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
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApprovedOrderCoverSheet;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.APPROVED_ORDER_COVER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocmosisApprovedOrderCoverSheetService {
    private static final String ANNEX_A = "ANNEX A:";
    private static final String ANNEX_A_WEL = "Atodiad A:";

    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DocumentMerger documentMerger;
    private final Time time;

    public DocmosisDocument addCoverSheetToApprovedOrder(CaseData caseData, DocumentReference order,
                                                         Element<HearingOrder> hearingOrderElement) throws IOException {
        // Create
        DocmosisDocument coverSheet = createCoverSheet(caseData, hearingOrderElement);

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
                contentStream.showText(getAnnexText(caseData));
                contentStream.endText();

                // Calculate underline position and length, font size is 12
                float textWidth = PDType1Font.TIMES_BOLD.getStringWidth(getAnnexText(caseData)) / 1000 * 12;
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

    public DocmosisDocument createCoverSheet(CaseData caseData, Element<HearingOrder> hearingOrder) {
        DocmosisApprovedOrderCoverSheet coverDocumentData = buildCoverDocumentsData(caseData, hearingOrder);
        return docmosisDocumentGeneratorService.generateDocmosisDocument(coverDocumentData,
            APPROVED_ORDER_COVER,
            RenderFormat.PDF,
            getCaseLanguage(caseData));
    }

    public DocmosisApprovedOrderCoverSheet buildCoverDocumentsData(CaseData caseData,
                                                                   Element<HearingOrder> hearingOrder) {

        return DocmosisApprovedOrderCoverSheet.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(caseDataExtractionService.getCourtName(caseData))
            .children(caseDataExtractionService.getChildrenDetails(caseData.getAllChildren()))
            .judgeTitleAndName(caseData.getReviewDraftOrdersData().getJudgeTitleAndName())
            .dateOfApproval(formatLocalDateToString(time.now().toLocalDate(), DATE, getCaseLanguage(caseData)))
            .crest(CREST.getValue())
            .orderByConsent(isC2OrderByConsent(caseData, hearingOrder) ? YES.getValue() : null)
            .build();
    }

    private String getAnnexText(CaseData caseData) {
        return WELSH.equals(getCaseLanguage(caseData)) ? ANNEX_A_WEL : ANNEX_A;
    }

    private Language getCaseLanguage(CaseData caseData) {
        return Optional.ofNullable(caseData.getC110A().getLanguageRequirementApplication()).orElse(Language.ENGLISH);
    }

    private boolean isC2OrderByConsent(CaseData caseData, Element<HearingOrder> hearingOrder) {
        // 1. filter out all additional applications bundles by consent
        // 2. check if the hearing order id is present in any of the bundle
        return unwrapElements(caseData.getAdditionalApplicationsBundle()).stream()
            .anyMatch(additionalApplicationsBundle ->
                Stream.of(additionalApplicationsBundle.getC2DocumentBundle(),
                        additionalApplicationsBundle.getC2DocumentBundleConfidential())
                    .filter(Objects::nonNull)
                    .filter(documentBundle -> WITHOUT_NOTICE.equals(documentBundle.getType()))
                    .map(C2DocumentBundle::getDraftOrdersBundle)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .anyMatch(draftOrderElement ->
                        draftOrderElement.getValue().getDocument().getUrl()
                            .equals(hearingOrder.getValue().getOrderOrOrderConfidential().getUrl()))
            );
    }
}
