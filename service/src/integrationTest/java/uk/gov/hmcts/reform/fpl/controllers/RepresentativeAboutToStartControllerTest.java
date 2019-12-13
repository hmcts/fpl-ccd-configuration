package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class RepresentativeAboutToStartControllerTest extends AbstractControllerTest {

    public RepresentativeAboutToStartControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldPopulateExistingRepresentativesRespondentsAndOthers() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .email("john.smith@test.com")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(EMAIL)
            .build();

        List<Element<Representative>> representatives = ElementUtils.wrap(representative);

        Map<String, Object> incomingCaseDate = ImmutableMap.of(
            "representatives", representatives,
            "respondents1", createRespondents(),
            "others", createOthers()
        );

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStart(incomingCaseDate);

        CaseData outgoingCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(outgoingCaseData.getRepresentatives()).isEqualTo(representatives);
        assertThat(String.valueOf(callbackResponse.getData().get("others_label"))).contains(
            "Person 1 - Kyle Stafford",
            "Other person 1 - Sarah Simpson");
        assertThat(String.valueOf(callbackResponse.getData().get("respondents_label"))).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson");
    }

    @Test
    void shouldPrePopulateRepresentatives() {
        Map<String, Object> incomingCaseData = ImmutableMap.of();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStart(incomingCaseData);

        CaseData outgoingCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        Representative representative = Representative.builder().build();
        List<Element<Representative>> expectedRepresentatives = ElementUtils.wrap(representative);

        assertThat(outgoingCaseData.getRepresentatives()).isEqualTo(expectedRepresentatives);
        assertThat(String.valueOf(callbackResponse.getData().get("others_label"))).isEqualTo("No others on the case");
        assertThat(String.valueOf(callbackResponse.getData().get("respondents_label"))).isEqualTo("No respondents on the case");
    }

}
