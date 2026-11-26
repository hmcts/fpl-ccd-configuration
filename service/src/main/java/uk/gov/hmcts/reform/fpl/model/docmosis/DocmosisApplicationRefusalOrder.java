package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode()
@Builder
public class DocmosisApplicationRefusalOrder implements DocmosisData {
    private final String familyManCaseNumber;
    private final String courtName;
    private final List<DocmosisChild> children;
    private final String judgeTitleAndName;
    private final String dateOfRefusal;
    private final String crest;

    private final String applicationDate;
    private final String refusalReason;
}
