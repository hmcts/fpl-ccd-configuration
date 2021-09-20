package uk.gov.hmcts.reform.fpl.enums.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RenderFormatTest {

    @Test
    void testFromNonRecognised() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> RenderFormat.fromFileName("a.txt"));

        assertThat(exception.getMessage()).isEqualTo("Extension txt not recognised for rendering");
    }

    @ParameterizedTest
    @MethodSource("fileAndTypes")
    void testFrom(String filename, RenderFormat expected) {
        assertThat(RenderFormat.fromFileName(filename)).isEqualTo(expected);
    }

    private static Stream<Arguments> fileAndTypes() {
        return Stream.of(
            Arguments.of("x.doc", RenderFormat.WORD),
            Arguments.of("z.docx", RenderFormat.DOC_X),
            Arguments.of("y.pdf", RenderFormat.PDF)
        );
    }
}
