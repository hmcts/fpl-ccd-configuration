package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class HearingVenueLookUpService {

    private final ObjectMapper objectMapper;

    public HearingVenueLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private List<HearingVenue> getHearingVenueMappings() throws IOException {
        String json = ResourceReader.readString("static_data/hearingVenues.json");
        return objectMapper.reader()
            .forType(new TypeReference<List<HearingVenue>>() {})
            .readValue(json);
    }

    HearingVenue getHearingVenue(final String venueId) throws IOException {
        List<HearingVenue> hearingVenues = getHearingVenueMappings();

        return hearingVenues.stream()
            .filter(hearingVenue -> venueId.equalsIgnoreCase(hearingVenue.getHearingVenueId()))
            .findFirst()
            .orElse(HearingVenue.builder().build());
    }
}
