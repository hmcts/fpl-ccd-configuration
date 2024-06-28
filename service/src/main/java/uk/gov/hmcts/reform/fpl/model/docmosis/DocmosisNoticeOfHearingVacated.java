package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class DocmosisNoticeOfHearingVacated implements DocmosisData {
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;
    private final String crest;

    @JsonUnwrapped
    private DocmosisHearingBooking hearingBooking;
    private final String vacatedDate;
    private final String vacatedReason;
    private final String relistAction;
}
