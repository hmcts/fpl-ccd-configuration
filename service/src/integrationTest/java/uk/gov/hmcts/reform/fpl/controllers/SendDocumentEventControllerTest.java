package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParty;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class SendDocumentEventControllerTest extends AbstractControllerTest {

    @MockBean
    private Clock clock;

    SendDocumentEventControllerTest() {
        super("send-document");
    }

    @BeforeEach
    void setupStoppedClock() {
        when(clock.getZone()).thenReturn(UTC.normalized());
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 5, 12, 10).toInstant(UTC));
    }

    @Test
    void shouldSendDocumentToRepresentativesWithPostServingPreferences() {

        Representative representative1 = representative("John Smith", POST);
        Representative representative2 = representative("Alex Brown", EMAIL);
        Representative representative3 = representative("Emma White", DIGITAL_SERVICE);

        DocumentReference documentToBeSent = testDocument();

        CaseDetails caseDetails = buildCaseData(documentToBeSent, representative1, representative2, representative3);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        List<DocumentsSentToParty> outgoingCaseData = unwrapElements(mapper.convertValue(
            callbackResponse.getData().get("documentsSentToParties"), new TypeReference<>() {
            }));

        assertThat(outgoingCaseData).hasSize(1);
        assertThat(outgoingCaseData.get(0).getPartyName()).isEqualTo("John Smith");
        assertThat(unwrapElements(outgoingCaseData.get(0).getDocumentsSentToParty()))
            .containsExactly(DocumentSentToParty.builder()
                .partyName(representative1.getFullName())
                .document(documentToBeSent)
                .coversheet(documentToBeSent)
                .sentAt("12:10pm, 5 January 2020")
                .build());
    }

    private static CaseDetails buildCaseData(DocumentReference documentToBeSent, Representative... representatives) {
        return CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "documentToBeSent", documentToBeSent,
                "representatives", ElementUtils.wrapElements(representatives)))
            .build();
    }

    private static Representative representative(String name, RepresentativeServingPreferences servingPreferences) {
        return Representative.builder()
            .fullName(name)
            .positionInACase("Position")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(servingPreferences)
            .build();
    }
}
