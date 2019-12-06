package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
public class HearingVenueLookUpService {

    private final ObjectMapper objectMapper;
    private List<HearingVenue> hearingVenues;

    @Autowired
    public HearingVenueLookUpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        populateHearingVenueMappings();
    }

    public HearingDateDynamicElement getHearingDynamicElement(DynamicList list) {
        return HearingDateDynamicElement.builder()
            .date(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .build();
    }

    private void populateHearingVenueMappings() {
        if (isEmpty(hearingVenues)) {
            try {
                final String jsonContent = ResourceReader.readString("static_data/hearingVenues.json");
                hearingVenues = objectMapper.reader()
                    .forType(new TypeReference<List<HearingVenue>>() {})
                    .readValue(jsonContent);

            } catch (IOException e) {
                log.error("Unable to parse hearingVenues.json file.", e);
            }
        }
    }

    HearingVenue getHearingVenue(final String venueId) {
        return this.hearingVenues.stream()
            .filter(hearingVenue -> venueId.equalsIgnoreCase(hearingVenue.getHearingVenueId()))
            .findFirst()
            .orElse(HearingVenue.builder().build());
    }

    String buildHearingVenue(final HearingVenue hearingVenue) {
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
