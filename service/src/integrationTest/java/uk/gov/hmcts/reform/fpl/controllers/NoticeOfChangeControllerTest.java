package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.RespondentSolicitorNoticeOfChangeTemplate;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 10L;

    @Captor
    private ArgumentCaptor<CallbackRequest> requestCaptor;

    @MockBean
    private CoreCaseDataApiV2 caseDataApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private NotificationClient notificationClient;

    NoticeOfChangeControllerTest() {
        super("noc-decision");
    }

    @Nested
    class AboutToStart {
        final String solicitorId = "1111111";
        final Organisation newOrganisation = organisation("NEW_ORG");
        final AuditEventsResponse auditEvents = AuditEventsResponse.builder()
            .auditEvents(List.of(AuditEvent.builder()
                .userId(solicitorId)
                .id("nocRequest")
                .build()))
            .build();

        final UserDetails solicitorUser = UserDetails.builder()
            .forename("Emma")
            .surname("Willson")
            .email("emma.willson@test.com")
            .id(solicitorId)
            .build();

        final AboutToStartOrSubmitCallbackResponse assignmentResponse = AboutToStartOrSubmitCallbackResponse
            .builder()
            .build();

        @BeforeEach
        void init() {
            givenFplService();
            givenSystemUser();
            when(caseDataApi.getAuditEvents(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, false, CASE_ID.toString()))
                .thenReturn(auditEvents);
            when(idamClient.getUserByUserId(USER_AUTH_TOKEN, solicitorId))
                .thenReturn(solicitorUser);
            when(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), requestCaptor.capture()))
                .thenReturn(assignmentResponse);
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
                .organisationToAdd(newOrganisation)
                .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .respondents1(List.of(respondent1, respondent2, respondent3))
                .changeOrganisationRequestField(changeRequest)
                .build();

            final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

            final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue());

            final Element<Respondent> expectedRespondent = update(respondent2, solicitorUser, newOrganisation);

            assertThat(actualResponse).isEqualTo(assignmentResponse);
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
                .organisationToAdd(newOrganisation)
                .caseRoleId(caseRoleDynamicList("[SOLICITORA]"))
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .changeOrganisationRequestField(changeRequest)
                .respondents1(List.of(respondent1, respondent2, respondent3))
                .build();


            final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToStartEvent(caseData);

            final CaseData updatedCaseData = extractCaseData(requestCaptor.getValue());

            final Element<Respondent> expectedRespondent = update(respondent1, solicitorUser, newOrganisation);

            assertThat(updatedCaseData.getRespondents1()).containsExactly(expectedRespondent, respondent2, respondent3);
            assertThat(actualResponse).isEqualTo(assignmentResponse);
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

    @Nested
    class Submitted {
        final String caseName = "Test";
        final String solicitorEmail = "solicitor@email.com";
        final String notificationReference = "localhost/" + CASE_ID;
        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(caseName)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .solicitor(RespondentSolicitor.builder()
                    .firstName("Old")
                    .lastName("Solicitor")
                    .email(solicitorEmail)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build())).build();

        private final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseName(caseName)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .solicitor(RespondentSolicitor.builder()
                    .firstName("New")
                    .lastName("Solicitor")
                    .email(solicitorEmail)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build())).build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(asCaseDetails(caseDataBefore))
            .caseDetails(asCaseDetails(caseData))
            .build();

        @Test
        void shouldNotifyNewRespondentSolicitorWhenCaseIsSubmitted() {

            RespondentSolicitorNoticeOfChangeTemplate respondentSolicitorNoticeOfChangeTemplate =
                getExpectedNoticeOfChangeParameters()
                    .salutation("Dear New Solicitor")
                    .caseUrl("http://fake-url/cases/case-details/" + CASE_ID)
                    .build();

            final Map<String, Object> registeredSolicitorParameters = mapper.convertValue(
                respondentSolicitorNoticeOfChangeTemplate, new TypeReference<>() {
                });

            postSubmittedEvent(callbackRequest);

            checkUntil(() ->
                verify(notificationClient).sendEmail(
                    NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
                    solicitorEmail,
                    registeredSolicitorParameters,
                    notificationReference));
        }

        @Test
        void shouldNotifyOldRespondentSolicitorWhenCaseIsSubmitted() {

            RespondentSolicitorNoticeOfChangeTemplate respondentSolicitorNoticeOfChangeTemplate =
                getExpectedNoticeOfChangeParameters()
                    .salutation("Dear Old Solicitor")
                    .build();

            final Map<String, Object> registeredSolicitorParameters = mapper.convertValue(
                respondentSolicitorNoticeOfChangeTemplate, new TypeReference<>() {
                });

            postSubmittedEvent(callbackRequest);

            checkUntil(() ->
                verify(notificationClient).sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    solicitorEmail,
                    registeredSolicitorParameters,
                    notificationReference));
        }

        private RespondentSolicitorNoticeOfChangeTemplate.RespondentSolicitorNoticeOfChangeTemplateBuilder
            getExpectedNoticeOfChangeParameters() {
            return RespondentSolicitorNoticeOfChangeTemplate.builder()
                .caseName(caseName)
                .ccdNumber(CASE_ID.toString());
        }
    }
}
