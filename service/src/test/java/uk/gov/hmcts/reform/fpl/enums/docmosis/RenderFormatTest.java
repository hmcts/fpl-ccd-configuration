package uk.gov.hmcts.reform.fpl.enums.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.DOC_X;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.WORD;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.fromFileName;

class RenderFormatTest {

    @Test
    void testFromNonRecognised() {
        assertThatThrownBy(() -> fromFileName("a.txt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Extension 'txt' is not recognised for rendering");
    }

    @ParameterizedTest
    @MethodSource("extensionToFormat")
    void testFrom(String filename, RenderFormat expected) {
        assertThat(fromFileName(filename)).isEqualTo(expected);
    }

    private static Stream<Arguments> extensionToFormat() {
        return Stream.of(
            Arguments.of("x.doc", WORD), Arguments.of("x.DOC", WORD), Arguments.of("x.DoC", WORD),
            Arguments.of("z.docx", DOC_X), Arguments.of("z.DOCX", DOC_X), Arguments.of("z.DocX", DOC_X),
            Arguments.of("y.pdf", PDF), Arguments.of("y.PDF", PDF), Arguments.of("y.PdF", PDF)
        );
    }
}
