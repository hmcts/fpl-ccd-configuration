package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.AuditEvent;
import uk.gov.hmcts.reform.fpl.model.AuditEventsResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 10L;
    private static final String SOLICITOR_ID = "1111111";

    private static final Organisation NEW_ORGANISATION = organisation("NEW_ORG");

    private static final AuditEventsResponse AUDIT_EVENTS = AuditEventsResponse.builder()
        .auditEvents(List.of(AuditEvent.builder()
            .userId(SOLICITOR_ID)
            .id("nocRequest")
            .build()))
        .build();

    private static final UserDetails SOLICITOR_USER = UserDetails.builder()
        .forename("Emma")
        .surname("Willson")
        .email("emma.willson@test.com")
        .id(SOLICITOR_ID)
        .build();

    private static final AboutToStartOrSubmitCallbackResponse ASSIGNMENT_RESPONSE = AboutToStartOrSubmitCallbackResponse
        .builder()
        .build();

    @Captor
    private ArgumentCaptor<CallbackRequest> requestCaptor;

    @MockBean
    private CoreCaseDataApiV2 caseDataApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    NoticeOfChangeControllerTest() {
        super("noc-decision");
    }

    @BeforeEach
    void init() {
        givenFplService();
        givenSystemUser();
        when(caseDataApi.getAuditEvents(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, false, CASE_ID.toString()))
            .thenReturn(AUDIT_EVENTS);
        when(idamClient.getUserByUserId(USER_AUTH_TOKEN, SOLICITOR_ID))
            .thenReturn(SOLICITOR_USER);
        when(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), requestCaptor.capture()))
            .thenReturn(ASSIGNMENT_RESPONSE);
    }

    @Test
    void shouldAddRespondentRepresentation() {

        final Element<Respondent> respondent1 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("First")
                .lastName("Respondent")
                .build())
            .build());

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Second")
                .lastName("Respondent")
                .build())
            .build());

        final Element<Respondent> respondent3 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Third")
                .lastName("Respondent")
                .build())
            .build());

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(List.of(respondent1, respondent2, respondent3))
            .changeOrganisationRequestField(changeRequest)
            .build();

        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue());

        final Element<Respondent> expectedRespondent = update(respondent2, SOLICITOR_USER, NEW_ORGANISATION);

        assertThat(actualResponse).isEqualTo(ASSIGNMENT_RESPONSE);
        assertThat(updatedCaseData.getRespondents1()).containsExactly(respondent1, expectedRespondent, respondent3);
    }

    @Test
    void shouldUpdateRespondentRepresentation() {

        final Element<Respondent> respondent1 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("First")
                .lastName("Respondent")
                .build())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Tim")
                .lastName("Brown")
                .email("tim.brown@test.com")
                .organisation(organisation("OLD_ORG"))
                .build())
            .build());

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Second")
                .lastName("Respondent")
                .build())
            .build());

        final Element<Respondent> respondent3 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Third")
                .lastName("Respondent")
                .build())
            .build());

        final ChangeOrganisationRequest changeRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(NEW_ORGANISATION)
            .caseRoleId(caseRoleDynamicList("[SOLICITORA]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .changeOrganisationRequestField(changeRequest)
            .respondents1(List.of(respondent1, respondent2, respondent3))
            .build();


        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

        final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue());

        final Element<Respondent> expectedRespondent = update(respondent1, SOLICITOR_USER, NEW_ORGANISATION);

        assertThat(updatedCaseData.getRespondents1()).containsExactly(expectedRespondent, respondent2, respondent3);
        assertThat(actualResponse).isEqualTo(ASSIGNMENT_RESPONSE);
    }

    private Element<Respondent> update(Element<Respondent> respondent, UserDetails solicitor, Organisation org) {
        return element(respondent.getId(), respondent.getValue().toBuilder()
            .legalRepresentation("Yes")
            .solicitor(RespondentSolicitor.builder()
                .firstName(solicitor.getForename())
                .lastName(solicitor.getSurname().orElse(null))
                .email(solicitor.getEmail())
                .organisation(org)
                .build())
            .build());
    }

}
