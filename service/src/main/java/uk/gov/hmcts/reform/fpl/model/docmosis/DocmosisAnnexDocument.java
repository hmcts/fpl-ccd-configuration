package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocmosisAnnexDocument {
    String title;
    String description;
}
