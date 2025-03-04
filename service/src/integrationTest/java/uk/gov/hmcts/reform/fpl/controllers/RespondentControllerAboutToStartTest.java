package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority.DUMMY_UUID;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private RepresentativeService representativeService;

    RespondentControllerAboutToStartTest() {
        super("enter-respondents");
    }

    @Test
    void aboutToStartShouldPrePopulateRespondent() {
        when(representativeService.shouldUserHaveAccessToRespondentsChildrenEvent(any())).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("respondents1");
        assertThat(callbackResponse.getData()).doesNotContainKey("respondentLocalAuthority");
    }

    @Test
    void shouldPopulateRespondentLA() {
        CaseData caseData = CaseData.builder()
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondentLocalAuthority(RespondentLocalAuthority.builder().build())
            .respondents1(List.of(element(DUMMY_UUID, Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Swansea County Council")
                    .address(Address.builder().addressLine1("addr1").build())
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .email("test@test.com")
                    .telephoneNumber(Telephone.builder().telephoneNumber("123").build())
                    .organisation(Organisation.builder()
                        .organisationName("Swansea")
                        .organisationID("test")
                        .build())
                    .firstName("John")
                    .lastName("Smith")
                    .build())
                .usingOtherOrg(YesNo.NO)
                .isLocalAuthority(YesNo.YES)
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData after = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(after).extracting("respondentLocalAuthority")
            .isEqualTo(RespondentLocalAuthority.builder()
                .name("Swansea County Council")
                .address(Address.builder().addressLine1("addr1").build())
                .email("test@test.com")
                .representativeFirstName("John")
                .representativeLastName("Smith")
                .organisation(Organisation.builder()
                    .organisationName("Swansea")
                    .organisationID("test")
                    .build())
                .phoneNumber("123")
                .usingOtherOrg(YesNo.NO)
                .build());
        assertThat(callbackResponse.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldThrowErrorIfUserRestrictedFromAccessingEvent() {
        when(representativeService.shouldUserHaveAccessToRespondentsChildrenEvent(any())).thenReturn(false);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors())
            .contains("Contact the applicant or CTSC to modify respondent details.");
    }
}
