package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseByEmailContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class,
    DateFormatterService.class, HearingBookingService.class, PartyAddedToCaseByEmailContentProvider.class})
class PartyAddedToCaseByEmailContentProviderTest {

    @Autowired
    private HearingBookingService hearingBookingService;

    private DateFormatterService dateFormatterService;

    private PartyAddedToCaseByEmailContentProvider partyAddedToCaseByEmailContentProvider;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        this.partyAddedToCaseByEmailContentProvider = new PartyAddedToCaseByEmailContentProvider("", mapper,
            dateFormatterService, hearingBookingService);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Smith")
            .put("familyManCaseNumber", "12345L")
            .build();

        assertThat(partyAddedToCaseByEmailContentProvider.buildPartyAddedToCaseNotification(
            callbackRequest().getCaseDetails()))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "")
            .put("familyManCaseNumber", "")
            .build();

        assertThat(partyAddedToCaseByEmailContentProvider.buildPartyAddedToCaseNotification(emptyCaseDetails()))
            .isEqualTo(expectedParameters);
    }
}
