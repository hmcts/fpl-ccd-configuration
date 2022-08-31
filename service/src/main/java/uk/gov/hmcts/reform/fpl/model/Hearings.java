package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Jacksonized
public class Hearings {
    private List<Element<HearingBooking>> hearingDetails;
}
