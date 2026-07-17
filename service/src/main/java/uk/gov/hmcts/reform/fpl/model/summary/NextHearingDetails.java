package uk.gov.hmcts.reform.fpl.model.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class NextHearingDetails {

    @CCD(label = "Next hearing date", searchable = false)
    LocalDateTime hearingDateTime;

}
