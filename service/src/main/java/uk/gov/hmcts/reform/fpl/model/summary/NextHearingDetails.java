package uk.gov.hmcts.reform.fpl.model.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class NextHearingDetails {

    LocalDateTime hearingDateTime;

}
