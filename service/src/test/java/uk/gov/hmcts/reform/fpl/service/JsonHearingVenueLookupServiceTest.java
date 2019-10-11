package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, JsonHearingVenueLookupService.class})
public class JsonHearingVenueLookupServiceTest {

    private static final int EXPECTED_SIZE = 88;

    @Autowired
    private HearingVenueLookupService lookupService;

    @Test
    void getHearingVenues() throws IOException {
        List<HearingVenue> hearingVenues = lookupService.getHearingVenues();

        // Assert that the correct number have been pulled
        assertEquals(EXPECTED_SIZE, hearingVenues.size());

        // Check that some of the elements are correct, scattered throughout the file
        assertVenue(1, "CARDIFF ABERDARE HEARING CENTRE", hearingVenues.get(0));
        assertVenue(44, "PORTSMOUTH WINCHESTER COMBINED COURT CENTRE", hearingVenues.get(43));
        assertVenue(88, "WORCESTER WORCESTER JUSTICE CENTRE", hearingVenues.get(87));
    }

    private void assertVenue(int id, String title, HearingVenue actual) {
        HearingVenue venue = HearingVenue.builder()
            .hearingVenueId(id)
            .title(title)
            .build();

        assertEquals(venue, actual);
    }
}
