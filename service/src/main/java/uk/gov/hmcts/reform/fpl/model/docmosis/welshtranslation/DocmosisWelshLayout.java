package uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisWelshLayout {
    private final boolean bilingual;
    private final boolean mirrorImage;
    private final boolean other;
}
