package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Slf4j
@Service
public class HearingVenueLookUpService {
    public static final String HEARING_VENUE_ID_OTHER = "OTHER";

    private final ObjectMapper objectMapper;
    private List<HearingVenue> hearingVenues;

    @Autowired
    public HearingVenueLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadHearingVenueMappings();
    }

    private void loadHearingVenueMappings() {
        try {
            final String jsonContent = ResourceReader.readString("static_data/hearingVenues.json");
            hearingVenues = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse hearingVenues.json file.", e);
        }
    }

    public HearingVenue getHearingVenue(final HearingBooking hearingBooking) {
        if (!HEARING_VENUE_ID_OTHER.equals(hearingBooking.getVenue())) {
            return getHearingVenue(hearingBooking.getVenue());
        } else {
            return HearingVenue.builder()
                .hearingVenueId(HEARING_VENUE_ID_OTHER)
                .venue("Other")
                .address(hearingBooking.getVenueCustomAddress())
                .build();
        }
    }

    private HearingVenue getHearingVenue(final String venueId) {
        return this.hearingVenues.stream()
            .filter(hearingVenue -> venueId.equalsIgnoreCase(hearingVenue.getHearingVenueId()))
            .findFirst()
            .orElse(HearingVenue.builder().build());
    }

    public String getVenueId(final String venueAsString) {
        for (HearingVenue hearingVenue : hearingVenues) {
            if (venueAsString.equalsIgnoreCase(buildHearingVenue(hearingVenue))) {
                return hearingVenue.getHearingVenueId();
            }
        }
        return HEARING_VENUE_ID_OTHER;
    }

    public String buildHearingVenue(final HearingVenue hearingVenue) {
        if (hearingVenue == null || hearingVenue.getAddress() == null) {
            return "";
        } else {
            return Stream.of(hearingVenue.getAddress().getAddressLine1(), hearingVenue.getAddress().getAddressLine2(),
                hearingVenue.getAddress().getPostTown(), hearingVenue.getAddress().getPostcode())
                .filter(StringUtils::isNotBlank)
                .collect(joining(", "));
        }
    }
}
