package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;

@Service
public class JsonHearingVenueLookupService implements HearingVenueLookupService {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonHearingVenueLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<HearingVenue> getHearingVenues() throws IOException {
        String jsonContent = ResourceReader.readString("static_data/hearingVenues.json");
        return objectMapper.readValue(jsonContent, new TypeReference<List<HearingVenue>>() {
        });
    }
}
