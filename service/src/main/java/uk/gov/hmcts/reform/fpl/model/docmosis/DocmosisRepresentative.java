package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocmosisRepresentative {
    private final String name;
    private final List<DocmosisRepresentedBy> representedBy;
}
