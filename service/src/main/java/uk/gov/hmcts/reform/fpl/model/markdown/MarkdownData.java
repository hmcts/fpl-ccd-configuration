package uk.gov.hmcts.reform.fpl.model.markdown;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class MarkdownData {
    private final String header;
    private final String body;
}
