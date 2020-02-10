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
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseThroughDigitalServiceContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class,
    DateFormatterService.class, HearingBookingService.class,
    PartyAddedToCaseThroughDigitalServiceContentProvider.class})
class PartyAddedToCaseThroughDigitalServiceContentProviderTest {

    @Autowired
    private HearingBookingService hearingBookingService;

    private DateFormatterService dateFormatterService;

    private PartyAddedToCaseThroughDigitalServiceContentProvider partyAddedToCaseThroughDigitalServiceContentProvider;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        this.partyAddedToCaseThroughDigitalServiceContentProvider =
            new PartyAddedToCaseThroughDigitalServiceContentProvider("", mapper,
            dateFormatterService, hearingBookingService);
    }

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Smith")
            .put("familyManCaseNumber", "12345L")
            .put("caseUrl", "/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345")
            .build();

        assertThat(partyAddedToCaseThroughDigitalServiceContentProvider
            .buildPartyAddedToCaseNotification(callbackRequest().getCaseDetails()))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "")
            .put("familyManCaseNumber", "")
            .put("caseUrl", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/123")
            .build();

        assertThat(partyAddedToCaseThroughDigitalServiceContentProvider
            .buildPartyAddedToCaseNotification(emptyCaseDetails()))
            .isEqualTo(expectedParameters);
    }
}
