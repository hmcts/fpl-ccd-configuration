package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class RepresentativeAboutToStartControllerTest extends AbstractCallbackTest {

    RepresentativeAboutToStartControllerTest() {
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

        List<Element<Representative>> representatives = wrapElements(representative);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "representatives", representatives,
                "respondents1", createRespondents(),
                "others", createOthers()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

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
    void shouldPopulateExistingInactiveRespondentsAndInactiveOthers() {
        Respondent activeRespondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("Timothy").lastName("Jones").build())
            .activeParty(YES.getValue())
            .build();

        Respondent inactiveRespondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("Sarah").lastName("Simpson").build())
            .activeParty(NO.getValue())
            .build();

        Others others = Others.builder()
            .firstOther(Other.builder().name("Kyle Stafford").activeParty(YES.getValue()).build())
            .additionalOthers(wrapElements(Other.builder().name("Sarah Simpson").activeParty(NO.getValue()).build()))
            .build();


        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "respondents1", wrapElements(activeRespondent, inactiveRespondent),
                "others", others))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(String.valueOf(callbackResponse.getData().get("others_label"))).contains(
            "Person 1 - Kyle Stafford",
            "Other person 1 - Sarah Simpson - Inactive");
        assertThat(String.valueOf(callbackResponse.getData().get("respondents_label"))).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson - Inactive");
    }

    @Test
    void shouldPrePopulateRepresentatives() {
        CaseDetails caseDetails = CaseDetails.builder().id(RandomUtils.nextLong()).data(emptyMap()).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        CaseData outgoingCaseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        Representative representative = Representative.builder().build();

        assertThat(outgoingCaseData.getRepresentatives()).isEqualTo(wrapElements(representative));
        assertThat(String.valueOf(callbackResponse.getData().get("others_label")))
            .isEqualTo("No others on the case");
        assertThat(String.valueOf(callbackResponse.getData().get("respondents_label")))
            .isEqualTo("No respondents on the case");
    }
}
