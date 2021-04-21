package uk.gov.hmcts.reform.fpl.enums.docmosis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RenderFormat {
    WORD("doc", "application/msword"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String mediaType;
}
