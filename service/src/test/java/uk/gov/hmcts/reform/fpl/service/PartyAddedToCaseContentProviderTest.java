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
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseThroughDigitalServiceContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, PartyAddedToCaseContentProvider.class,
    DateFormatterService.class, HearingBookingService.class, PartyAddedToCaseThroughDigitalServiceContentProvider.class,
    PartyAddedToCaseByEmailContentProvider.class})
class PartyAddedToCaseContentProviderTest {

    @Autowired
    private HearingBookingService hearingBookingService;

    private DateFormatterService dateFormatterService;

    private PartyAddedToCaseByEmailContentProvider partyAddedToCaseByEmailContentProvider;

    private PartyAddedToCaseThroughDigitalServiceContentProvider partyAddedToCaseThroughDigitalServicelContentProvider;

    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        this.partyAddedToCaseByEmailContentProvider = new PartyAddedToCaseByEmailContentProvider("", mapper,
            dateFormatterService, hearingBookingService);

        this.partyAddedToCaseThroughDigitalServicelContentProvider
            = new PartyAddedToCaseThroughDigitalServiceContentProvider(
            "", mapper, dateFormatterService, hearingBookingService);

        this.partyAddedToCaseContentProvider = new PartyAddedToCaseContentProvider(
            "null", dateFormatterService, hearingBookingService, partyAddedToCaseByEmailContentProvider,
            partyAddedToCaseThroughDigitalServicelContentProvider);
    }

    @Test
    void shouldGetPartyAddedToCaseByEmailNotificationParameters() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Smith")
            .put("familyManCaseNumber", "12345L")
            .build();

        assertThat(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), EMAIL)).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGetPartyAddedToCaseThroughDigitalServiceNotificationParameters() throws IOException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Smith")
            .put("familyManCaseNumber", "12345L")
            .put("caseUrl", "/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345")
            .build();

        assertThat(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), DIGITAL_SERVICE)).isEqualTo(expectedParameters);
    }

    @Test
    void shouldGetPartyAddedToCaseByEmailNotificationTemplate() {
        assertThat(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationTemplate(EMAIL))
            .isEqualTo(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE);
    }

    @Test
    void shouldGetPartyAddedThroughDigitalServiceNotificationTemplate() {
        assertThat(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationTemplate(DIGITAL_SERVICE))
            .isEqualTo(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE);
    }
}
