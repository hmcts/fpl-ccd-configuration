package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UploadedOrderDocumentGeneratorTest {

    private static final String BINARY_URL = "binaryUrl";
    private static final byte[] BYTES = "content".getBytes();
    private static final String DOC_X_FILENAME = "max.docx";
    private static final CaseData CASE_DATA = CaseData.builder()
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersUploadOrderFile(DocumentReference.builder()
                .binaryUrl(BINARY_URL)
                .filename(DOC_X_FILENAME)
                .build())
            .build())
        .build();
    private static final byte[] CONVERTED_BYTES = "ConvertedPdfContent".getBytes();
    private static final byte[] CONVERTED_AND_SEALED_BYTES = "ConvertedPdfContentAndSealed".getBytes();
    private final DocumentDownloadService documentDownloadService = mock(DocumentDownloadService.class);

    private final DocumentConversionService documentConversionService = mock(DocumentConversionService.class);
    private final DocumentSealingService documentSealingService = mock(DocumentSealingService.class);
    private final UploadedOrderDocumentGenerator underTest = new UploadedOrderDocumentGenerator(
        documentSealingService,
        documentConversionService,
        documentDownloadService
    );

    @BeforeEach
    void setUp() {
        when(documentDownloadService.downloadDocument(BINARY_URL)).thenReturn(BYTES);
    }

    @ParameterizedTest
    @MethodSource("nonPdfs")
    void generateWhenTargetFormatIsNotPdfIgnoreConversion(RenderFormat renderFormat) {

        OrderDocumentGeneratorResult actual = underTest.generate(
            CASE_DATA,
            OrderStatus.DRAFT,
            renderFormat
        );

        assertThat(actual).isEqualTo(OrderDocumentGeneratorResult.builder()
            .renderFormat(RenderFormat.DOC_X)
            .bytes(BYTES)
            .build());

        verifyNoInteractions(documentConversionService, documentSealingService);
    }

    @Test
    void testTargetPdfAndNoSealing() {

        when(documentConversionService.convertToPdf(BYTES, DOC_X_FILENAME)).thenReturn(CONVERTED_BYTES);

        OrderDocumentGeneratorResult actual = underTest.generate(
            CASE_DATA,
            OrderStatus.SEALED,
            RenderFormat.PDF
        );

        assertThat(actual).isEqualTo(OrderDocumentGeneratorResult.builder()
            .renderFormat(RenderFormat.PDF)
            .bytes(CONVERTED_BYTES)
            .build());

        verifyNoInteractions(documentSealingService);
    }

    @Test
    void testTargetPdfAndSealingNeeded() {

        when(documentConversionService.convertToPdf(BYTES, DOC_X_FILENAME)).thenReturn(CONVERTED_BYTES);
        when(documentSealingService.sealDocument(CONVERTED_BYTES)).thenReturn(CONVERTED_AND_SEALED_BYTES);

        OrderDocumentGeneratorResult actual = underTest.generate(
            CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersNeedSealing("Yes")
                    .manageOrdersUploadOrderFile(DocumentReference.builder()
                        .binaryUrl(BINARY_URL)
                        .filename(DOC_X_FILENAME)
                        .build())
                    .build())
                .build(),
            OrderStatus.SEALED,
            RenderFormat.PDF
        );

        assertThat(actual).isEqualTo(OrderDocumentGeneratorResult.builder()
            .renderFormat(RenderFormat.PDF)
            .bytes(CONVERTED_AND_SEALED_BYTES)
            .build());

    }

    @Test
    void testTargetPdfAndSealingNotNeeded() {

        when(documentConversionService.convertToPdf(BYTES, DOC_X_FILENAME)).thenReturn(CONVERTED_BYTES);

        OrderDocumentGeneratorResult actual = underTest.generate(
            CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .manageOrdersNeedSealing("No")
                    .manageOrdersUploadOrderFile(DocumentReference.builder()
                        .binaryUrl(BINARY_URL)
                        .filename(DOC_X_FILENAME)
                        .build())
                    .build())
                .build(),
            OrderStatus.SEALED,
            RenderFormat.PDF
        );

        assertThat(actual).isEqualTo(OrderDocumentGeneratorResult.builder()
            .renderFormat(RenderFormat.PDF)
            .bytes(CONVERTED_BYTES)
            .build());

        verifyNoInteractions(documentSealingService);
    }

    private static Stream<Arguments> nonPdfs() {
        return Arrays.stream(RenderFormat.values())
            .filter(renderFormat -> renderFormat != RenderFormat.PDF)
            .map(Arguments::of);
    }
}
