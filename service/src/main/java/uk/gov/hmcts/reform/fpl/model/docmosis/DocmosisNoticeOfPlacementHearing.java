package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class DocmosisNoticeOfPlacementHearing implements DocmosisData {

    private final DocmosisChild child;
    private String familyManCaseNumber;
    private String courtName;
    private String postingDate;

    private final String hearingDate;
    private final String hearingVenue;
    private final String hearingDuration;
    boolean isHighCourtCase;

    private final String draftbackground;
    private final String courtseal;
    private final String crest;
}
