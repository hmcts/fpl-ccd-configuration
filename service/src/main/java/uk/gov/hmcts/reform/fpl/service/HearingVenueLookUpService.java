package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class HearingVenueLookUpService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public HearingVenueLookUpService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    private List<HearingVenue> getHearingVenueMappings() {

        try {
            File hearingVenueJsonFile = resourceLoader.getResource(
                "classpath:static_data/hearingVenues.json").getFile();
            String json = Files.readString(hearingVenueJsonFile.toPath());
            return objectMapper.reader()
                .forType(new TypeReference<List<HearingVenue>>() {})
                .readValue(json);

        } catch (IOException e) {
            //should we stop processing ???
            log.error("Could not find hearing venue json mapping.");
        }

        return Collections.emptyList();
    }

    HearingVenue getHearingVenue(final String venueId) {
        List<HearingVenue> hearingVenues = getHearingVenueMappings();

        return hearingVenues.stream()
            .filter(hearingVenue -> venueId.equalsIgnoreCase(hearingVenue.getHearingVenueId()))
            .findFirst()
            .orElse(HearingVenue.builder().build());
    }
}
