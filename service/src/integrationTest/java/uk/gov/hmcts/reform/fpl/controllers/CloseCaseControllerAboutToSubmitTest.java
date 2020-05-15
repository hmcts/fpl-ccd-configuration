package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CloseCase;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.DEPRIVATION_OF_LIBERTY;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.WITHDRAWN;

@ActiveProfiles("integration-test")
@WebMvcTest(CloseCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class CloseCaseControllerAboutToSubmitTest extends AbstractControllerTest {

    CloseCaseControllerAboutToSubmitTest() {
        super("close-case");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToYesWhenDeprivationReasonIsSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "closeCase", Map.of("fullReason", DEPRIVATION_OF_LIBERTY)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry("deprivationOfLiberty", "Yes");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToNoWhenDeprivationReasonIsNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "closeCase", Map.of("fullReason", WITHDRAWN)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry("deprivationOfLiberty", "No");
    }

    @Test
    void shouldCleanCaseDataOfTransientFields() {
        Map<String, Object> closeCaseMap = Map.of(
            "date", LocalDate.of(2013, 2, 26),
            "fullReason", OTHER,
            "details", "Just give me a reason, just a little bit's enough"
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseDetails.builder()
            .data(Map.of("closeCase", closeCaseMap))
            .build());

        assertThat(response.getData()).doesNotContainKeys("closeCase", "close_case_label");
    }

    @Test
    void shouldPopulateTabObjectOfCloseCaseData() {
        Map<String, Object> closeCaseMap = Map.of(
            "date", LocalDate.of(2013, 2, 26),
            "fullReason", OTHER,
            "details", "Just a second we're not broken just bent, and we can learn to love again"
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseDetails.builder()
            .data(Map.of("closeCase", closeCaseMap))
            .build());

        CloseCase closeCase = CloseCase.builder()
            .date(LocalDate.of(2013, 2, 26))
            .showFullReason(YES)
            .reason(OTHER)
            .details("Just a second we're not broken just bent, and we can learn to love again")
            .build();

        assertThat(response.getData()).containsEntry("closeCaseTabField", mapper.convertValue(closeCase,
            new TypeReference<Map<String, Object>>() {}));
    }
}
