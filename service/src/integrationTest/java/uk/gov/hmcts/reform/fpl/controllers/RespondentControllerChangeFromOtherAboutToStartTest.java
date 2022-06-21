package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private RequestData requestData;

    RespondentControllerChangeFromOtherAboutToStartTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    @Test
    void aboutToStartShouldPrePopulateOthersList() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "others", createOthers()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("othersList");
    }

    @Test
    void aboutToStartShouldReturnErrorsWhenNoOthersInCase() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors().get(0)).isEqualTo("There is no other person in this case.");
    }
}
