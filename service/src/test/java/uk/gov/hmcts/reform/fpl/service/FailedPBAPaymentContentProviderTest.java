package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class,
    DateFormatterService.class, HearingBookingService.class})
class FailedPBAPaymentContentProviderTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+la@gmail.com";

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    private final DateFormatterService dateFormatterService = new DateFormatterService();
    private final HearingBookingService hearingBookingService = new HearingBookingService();

    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @BeforeEach
    void setup() {
        this.failedPBAPaymentContentProvider = new FailedPBAPaymentContentProvider("",
            hearingBookingService, dateFormatterService);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));
    }

    @Test
    void shouldReturnExpectedMapWithValidCtscNotificationParamaters() throws IOException {
        Map<String, Object> expectedMap = getExpectedCtscNotificationParameters();

        assertThat(failedPBAPaymentContentProvider.buildCtscNotificationParameters(populatedCaseDetails(),
            ApplicationType.C2_APPLICATION)).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithValidLANotificationParamaters() {
        Map<String, Object> expectedMap = Map.of("applicationType", "C110a");

        assertThat(failedPBAPaymentContentProvider.buildLANotificationParameters(
            ApplicationType.C110A_APPLICATION)).isEqualTo(expectedMap);
    }

    private Map<String, Object> getExpectedCtscNotificationParameters() {
        return Map.of("applicationType", "C2",
            "caseUrl", "/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345");
    }
}
