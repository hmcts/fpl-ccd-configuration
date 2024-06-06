package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class DocmosisNoticeOfHearingVacated implements DocmosisData {
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;

    @JsonUnwrapped
    private DocmosisHearingBooking hearingBooking;
    private final String vacatedDate;
    private final String vacatedReason;
    private final String relistAction;
}
