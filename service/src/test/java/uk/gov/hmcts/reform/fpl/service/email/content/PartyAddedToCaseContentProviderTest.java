package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PartyAddedToCaseContentProvider.class})
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class PartyAddedToCaseContentProviderTest {

    @Autowired
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

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
            .put("caseUrl", String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, "12345"))
            .build();

        assertThat(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), DIGITAL_SERVICE)).isEqualTo(expectedParameters);
    }
}
