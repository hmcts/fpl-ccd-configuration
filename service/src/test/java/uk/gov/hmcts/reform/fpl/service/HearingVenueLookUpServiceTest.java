package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, HearingVenueLookUpService.class})
class HearingVenueLookUpServiceTest {

    @Autowired
    private HearingVenueLookUpService hearingVenueLookUpService;

    @Nested
    class HearingVenueLookup {

        final HearingVenue predefinedHearingVenue = HearingVenue.builder()
            .venue("Venue")
            .hearingVenueId("Venue")
            .address(Address.builder()
                .addressLine1("Crown Building")
                .addressLine2("Aberdare Hearing Centre")
                .postTown("Aberdare")
                .postcode("CF44 7DW")
                .build())
            .build();

        @Test
        void shouldReturnCustomVenueWhenCustomVenueAddressProvided() {
            Address venueAddress = Address.builder()
                .addressLine1(randomAlphanumeric(10))
                .postcode(randomAlphanumeric(10))
                .build();

            HearingBooking hearingBooking = HearingBooking.builder()
                .venue("OTHER")
                .venueCustomAddress(venueAddress)
                .build();

            HearingVenue actualHearingVenue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

            HearingVenue expectedHearingVenue = HearingVenue.builder()
                .venue("Other")
                .hearingVenueId(hearingBooking.getVenue())
                .address(venueAddress)
                .build();

            assertThat(actualHearingVenue).isEqualTo(expectedHearingVenue);
        }

        @Test
        void shouldReturnHearingVenueByVenueId() {
            HearingBooking hearingBooking = HearingBooking.builder().venue("Venue").build();

            HearingVenue actualHearingVenue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

            assertThat(actualHearingVenue).isEqualTo(predefinedHearingVenue);
        }

        @Test
        void shouldReturnHearingVenueByVenueIdWhenCustomHearingVenueIsSelected() {

            HearingVenue otherHearingVenue = HearingVenue.builder()
                .venue("Other")
                .hearingVenueId("OTHER")
                .address(Address.builder()
                    .build())
                .build();

            HearingBooking hearingBooking = HearingBooking.builder()
                .venue("OTHER")
                .venueCustomAddress(Address.builder().build())
                .build();

            HearingVenue actualHearingVenue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

            assertThat(actualHearingVenue).isEqualTo(otherHearingVenue);
        }

        @Test
        void shouldReturnHearingVenueByVenueIdWithCaseInsensitiveMatching() {
            HearingBooking hearingBooking = HearingBooking.builder().venue("venue").build();

            HearingVenue actualHearingVenue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

            assertThat(actualHearingVenue).isEqualTo(predefinedHearingVenue);
        }
    }

    @Nested
    class HearingVenueFormatter {
        @Test
        void shouldReturnEmptyWhenHearingVenueIsNull() {
            assertThat(hearingVenueLookUpService.buildHearingVenue(null)).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenNoVenueAddress() {
            HearingVenue hearingVenue = HearingVenue.builder()
                .hearingVenueId(randomAlphanumeric(10))
                .venue(randomAlphanumeric(10))
                .build();

            String formattedHearingVenue = hearingVenueLookUpService.buildHearingVenue(hearingVenue);

            assertThat(formattedHearingVenue).isEmpty();
        }

        @Test
        void shouldFormatHearingVenueWithFullAddress() {
            Address venueAddress = Address.builder()
                .addressLine1(randomAlphanumeric(10))
                .addressLine2(randomAlphanumeric(10))
                .addressLine3(randomAlphanumeric(10))
                .postcode(randomAlphanumeric(10))
                .postTown(randomAlphanumeric(10))
                .county(randomAlphanumeric(10))
                .country(randomAlphanumeric(10))
                .build();

            HearingVenue hearingVenue = HearingVenue.builder()
                .address(venueAddress)
                .build();

            String actualFormattedHearingVenue = hearingVenueLookUpService.buildHearingVenue(hearingVenue);
            String expectedFormattedHearingVenue = String.format("%s, %s, %s, %s",
                venueAddress.getAddressLine1(),
                venueAddress.getAddressLine2(),
                venueAddress.getPostTown(),
                venueAddress.getPostcode());

            assertThat(actualFormattedHearingVenue).isEqualTo(expectedFormattedHearingVenue);
        }

        @Test
        void shouldFormatHearingVenueWithPartialAddress() {
            Address venueAddress = Address.builder()
                .addressLine1(randomAlphanumeric(10))
                .postcode(randomAlphanumeric(10))
                .build();

            HearingVenue hearingVenue = HearingVenue.builder()
                .address(venueAddress)
                .build();

            String actualFormattedHearingVenue = hearingVenueLookUpService.buildHearingVenue(hearingVenue);
            String expectedFormattedHearingVenue = String.format("%s, %s",
                venueAddress.getAddressLine1(),
                venueAddress.getPostcode());

            assertThat(actualFormattedHearingVenue).isEqualTo(expectedFormattedHearingVenue);
        }
    }

    @Test
    void shouldReturnVenueIdWhenValidVenueProvided() {
        assertThat(hearingVenueLookUpService.getVenueId(
            "Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")).isEqualTo("Venue");
    }

    @Test
    void shouldReturnOtherWhenNoVenueFound() {
        assertThat(hearingVenueLookUpService.getVenueId("Custom House, Custom Street")).isEqualTo("OTHER");
    }
}
