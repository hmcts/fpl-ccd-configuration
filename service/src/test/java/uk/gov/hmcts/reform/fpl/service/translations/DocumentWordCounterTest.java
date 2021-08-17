package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentWordCounterTest {

    private static final byte[] ORIGINAL_DOCUMENT_CONTENT = "Document".getBytes();
    private static final byte[] CONVERTED_DOCUMENT = "ConvertedDocument".getBytes();

    private final DocumentConversionService documentConversionService = mock(DocumentConversionService.class);
    private final DocmosisHelper docmosisHelper = mock(DocmosisHelper.class);

    private final DocumentWordCounter underTest = new DocumentWordCounter(
        documentConversionService,
        docmosisHelper
    );

    @ParameterizedTest
    @MethodSource("examples")
    void testCount(String content, long expectedCount) {
        mockConversionToContent(content);

        long actual = underTest.count(ORIGINAL_DOCUMENT_CONTENT);

        assertThat(actual).isEqualTo(expectedCount);
    }

    private static Stream<Arguments> examples() {
        return Stream.of(
            Arguments.of("Word", 1),
            Arguments.of("More words", 2),
            Arguments.of("    More         words     ", 2),
            Arguments.of("More,words", 2),
            Arguments.of("More.words", 2),
            Arguments.of("More;words", 2),
            Arguments.of("More!words!", 2),
            Arguments.of("More?words?", 2),
            Arguments.of("More words\n", 2),
            Arguments.of("More words\r", 2),
            Arguments.of("More words\r\n", 2),
            Arguments.of("More words\n\n", 2)
        );
    }

    private void mockConversionToContent(String content) {
        when(documentConversionService.convertToPdf(ORIGINAL_DOCUMENT_CONTENT, "toCalculate.pdf"))
            .thenReturn(CONVERTED_DOCUMENT);
        when(docmosisHelper.extractPdfContent(CONVERTED_DOCUMENT)).thenReturn(content);
    }
}
