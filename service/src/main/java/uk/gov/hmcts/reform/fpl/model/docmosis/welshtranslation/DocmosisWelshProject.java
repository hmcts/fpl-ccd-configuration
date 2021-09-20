package uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocmosisWelshProject {
    private final boolean ctsc;
    private final boolean reform;
    private final boolean digitalProject;
}
