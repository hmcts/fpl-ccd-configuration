package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(CloseCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class CloseCaseControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String FULL_REASON_KEY = "fullReason";
    private static final String DEPRIVATION_OF_LIBERTY_KEY = "deprivationOfLiberty";
    private static final String CLOSE_CASE_KEY = "closeCase";
    private static final String DATE_KEY = "date";
    private static final String DETAILS_KEY = "details";

    CloseCaseControllerAboutToSubmitTest() {
        super("close-case");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToYesWhenDeprivationReasonIsSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                CLOSE_CASE_KEY, Map.of(FULL_REASON_KEY, DEPRIVATION_OF_LIBERTY)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry(DEPRIVATION_OF_LIBERTY_KEY, "Yes");
    }

    @Test
    void shouldSetDeprivationOfLibertyFlagToNoWhenDeprivationReasonIsNotSet() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                CLOSE_CASE_KEY, Map.of(FULL_REASON_KEY, WITHDRAWN)
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).containsEntry(DEPRIVATION_OF_LIBERTY_KEY, "No");
    }

    @Test
    void shouldCleanCaseDataOfTransientFields() {
        Map<String, Object> closeCaseMap = Map.of(
            DATE_KEY, LocalDate.of(2013, 2, 26),
            FULL_REASON_KEY, OTHER,
            DETAILS_KEY, "Just give me a reason, just a little bit's enough"
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseDetails.builder()
            .data(Map.of(CLOSE_CASE_KEY, closeCaseMap))
            .build());

        assertThat(response.getData()).doesNotContainKeys(CLOSE_CASE_KEY, "close_case_label");
    }

    @Test
    void shouldPopulateTabObjectOfCloseCaseData() {
        Map<String, Object> closeCaseMap = Map.of(
            DATE_KEY, LocalDate.of(2013, 2, 26),
            FULL_REASON_KEY, OTHER,
            DETAILS_KEY, "Just a second we're not broken just bent, and we can learn to love again"
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(CaseDetails.builder()
            .data(Map.of(CLOSE_CASE_KEY, closeCaseMap))
            .build());

        CloseCase closeCase = CloseCase.builder()
            .date(LocalDate.of(2013, 2, 26))
            .showFullReason(YES)
            .reason(OTHER)
            .details("Just a second we're not broken just bent, and we can learn to love again")
            .build();

        assertThat(response.getData()).containsEntry("closeCaseTabField", mapper.convertValue(closeCase,
            new TypeReference<Map<String, Object>>() {
            }));
    }
}
