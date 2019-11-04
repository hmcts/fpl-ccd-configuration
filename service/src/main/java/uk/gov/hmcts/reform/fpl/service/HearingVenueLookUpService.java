package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Slf4j
@Service
public class HearingVenueLookUpService {

    private final ObjectMapper objectMapper;

    @Autowired
    public HearingVenueLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private List<HearingVenue> getHearingVenueMappings() throws IOException {
        final String jsonContent = ResourceReader.readString("static_data/hearingVenues.json");
        return objectMapper.reader()
            .forType(new TypeReference<List<HearingVenue>>() {})
            .readValue(jsonContent);
    }

    HearingVenue getHearingVenue(final String venueId) throws IOException {
        List<HearingVenue> hearingVenues = getHearingVenueMappings();

        return hearingVenues.stream()
            .filter(hearingVenue -> venueId.equalsIgnoreCase(hearingVenue.getHearingVenueId()))
            .findFirst()
            .orElse(HearingVenue.builder().build());
    }

    String buildHearingVenue(final HearingVenue hearingVenue) {
        if (hearingVenue == null || hearingVenue.getAddress() == null) {
            return "";
        } else {
            return String.join(", ",
                defaultIfNull(hearingVenue.getAddress().getAddressLine1(), ""),
                defaultIfNull(hearingVenue.getAddress().getAddressLine2(), ""),
                defaultIfNull(hearingVenue.getAddress().getPostTown(), ""),
                defaultIfNull(hearingVenue.getAddress().getPostcode(), ""));
        }
    }
}
