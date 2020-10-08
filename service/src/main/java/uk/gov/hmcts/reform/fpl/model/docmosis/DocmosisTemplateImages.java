package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisTemplateImages {
    private final String draftWaterMark;
    private final String courtseal;
    private final String crest;
}
