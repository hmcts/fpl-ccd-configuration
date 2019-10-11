package uk.gov.hmcts.reform.fpl.service;

import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;

import java.io.IOException;
import java.util.List;

public interface HearingVenueLookupService {
    List<HearingVenue> getHearingVenues() throws IOException;
}
