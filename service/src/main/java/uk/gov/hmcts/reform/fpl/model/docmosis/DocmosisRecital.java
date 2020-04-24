package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisRecital {
    private final String title;
    private final String body;
}
