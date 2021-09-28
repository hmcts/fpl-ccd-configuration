package uk.gov.hmcts.reform.fpl.enums.docmosis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

import static java.lang.String.format;

@Getter
@RequiredArgsConstructor
public enum RenderFormat {
    WORD("doc", "application/msword"),
    DOC_X("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String mediaType;

    public static RenderFormat fromFileName(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return Arrays.stream(RenderFormat.values())
            .filter(format -> format.getExtension().equalsIgnoreCase(extension))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(format(
                "Extension '%s' is not recognised for rendering", extension
            )));
    }
}
