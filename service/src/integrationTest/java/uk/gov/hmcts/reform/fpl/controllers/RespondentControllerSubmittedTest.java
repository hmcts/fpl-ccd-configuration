package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerSubmittedTest extends AbstractCallbackTest {

    private static final State NON_RESTRICTED_STATE = SUBMITTED;
    private static final String ORGANISATION_NAME = "org name";
    private static final String CHILD_SURNAME_1 = "last name";
    private static final String CASE_NAME = "case name";
    private static final String SOLICITOR_ORG_ID = "Organisation ID";
    private static final String SOLICITOR_EMAIL = "solicitor@email.com";
    private static final String CASE_ID = "1234567890123456";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    private final Organisation organisation1 = organisation("ORG_1");
    private final Organisation organisation2 = organisation("ORG_2");
    private final Organisation organisation3 = organisation("ORG_3");

    private final Element<Respondent> respondent1 = respondent(organisation1);
    private final Element<Respondent> respondent1Updated = respondent(respondent1, organisation3);
    private final Element<Respondent> respondent2 = respondent(organisation2);
    private final Element<Respondent> respondent2Updated = respondent(respondent2, null);
    private final List<Element<Respondent>> updatedRespondents = List.of(respondent1Updated, respondent2Updated);
    private final List<Element<Respondent>> respondents = List.of(respondent1, respondent2);
    private final CaseData nocCaseDataBefore = CaseData.builder()
        .id(10L)
        .state(SUBMITTED)
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .syntheticCaseSummary(SyntheticCaseSummary.builder()
            .caseSummaryCourtName(COURT_NAME)
            .build())
        .respondents1(respondents)
        .build();

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private OrganisationApi orgApi;

    @MockBean
    private NotificationClient notificationClient;

    RespondentControllerSubmittedTest() {
        super("enter-respondents");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
        givenSystemUser();
        when(orgApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, SOLICITOR_ORG_ID))
            .thenReturn(uk.gov.hmcts.reform.rd.model.Organisation.builder().name(ORGANISATION_NAME).build());

        final List<StartEventResponse> startEventResponses = IntStream.range(0, 2)
            .mapToObj(i -> RandomStringUtils.randomAlphanumeric(10))
            .map(token -> StartEventResponse.builder().eventId("updateRepresentation").token(token).build())
            .collect(toList());

        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(startEventResponses));
    }

    @Test
    void shouldPublishRespondentsUpdatedEventIfNotOpenState() {
        Respondent respondentWithRegisteredSolicitor = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(SOLICITOR_EMAIL)
                .organisation(Organisation.builder().organisationID("Registered Org ID").build())
                .build())
            .build();

        Respondent respondentWithUnregisteredSolicitor = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(SOLICITOR_EMAIL)
                .unregisteredOrganisation(UnregisteredOrganisation.builder()
                    .name("Unregistered Org")
                    .build())
                .build())
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .state(SUBMITTED)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryCourtName(COURT_NAME)
                .build())
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(wrapElements(respondentWithRegisteredSolicitor, respondentWithUnregisteredSolicitor))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE),
            eq(SOLICITOR_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE),
            eq(SOLICITOR_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    @Test
    void shouldNotPublishRespondentsUpdatedEventIfOpenState() {
        Respondent respondentWithRegisteredSolicitor = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(SOLICITOR_EMAIL)
                .organisation(Organisation.builder().organisationID("Registered Org ID").build())
                .build())
            .build();

        Respondent respondentWithUnregisteredSolicitor = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(SOLICITOR_EMAIL)
                .unregisteredOrganisation(UnregisteredOrganisation.builder()
                    .name("Unregistered Org")
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .state(OPEN)
            .respondents1(wrapElements(respondentWithRegisteredSolicitor, respondentWithUnregisteredSolicitor))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .build();

        postSubmittedEvent(caseData);

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldRevokeAccessAndSendNotificationsWhenLegalCounselRemoved() throws NotificationClientException {
        final String legalCounsellorId = "some id";
        final String legalCounsellorEmail = "some email";
        final List<Element<LegalCounsellor>> legalCounsellors = wrapElements(
            LegalCounsellor.builder()
                .firstName("first")
                .lastName("last")
                .email(legalCounsellorEmail)
                .userId(legalCounsellorId)
                .build()
        );

        CaseData caseDataBefore = CaseData.builder()
            .state(NON_RESTRICTED_STATE)
            .id(Long.valueOf(CASE_ID))
            .caseName(CASE_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder().lastName(CHILD_SURNAME_1).dateOfBirth(dateNow()).build())
                    .build()
            ))
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(Organisation.builder().organisationID("some other id").build())
                        .build())
                    .legalCounsellors(legalCounsellors)
                    .build()
            ))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(Organisation.builder().organisationID(SOLICITOR_ORG_ID).build())
                        .build())
                    .build()
            ))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        CaseAssignedUserRolesRequest revokeRequestPayload = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                CaseAssignedUserRoleWithOrganisation.builder()
                    .userId(legalCounsellorId)
                    .caseRole("[BARRISTER]")
                    .caseDataId(CASE_ID)
                    .build()
            ))
            .build();

        verify(caseAccessApi).removeCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, revokeRequestPayload);

        Map<String, Object> notifyData = Map.of(
            "caseName", CASE_NAME,
            "ccdNumber", "1234-5678-9012-3456",
            "childLastName", CHILD_SURNAME_1,
            "clientFullName", ORGANISATION_NAME,
            "salutation", "Dear first last"
        );

        verify(notificationClient).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE, legalCounsellorEmail, notifyData, "localhost/" + CASE_ID
        );
    }

    @Test
    void shouldUpdateRepresentativesAccess() {
        final ChangeOrganisationRequest expectedChange1 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .organisationToAdd(organisation3)
            .organisationToRemove(organisation1)
            .caseRoleId(caseRoleDynamicList(SOLICITORA))
            .requestTimestamp(now())
            .build();

        final ChangeOrganisationRequest expectedChange2 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .caseRoleId(caseRoleDynamicList(SOLICITORB))
            .organisationToRemove(organisation2)
            .requestTimestamp(now())
            .build();

        final CaseData caseData = nocCaseDataBefore.toBuilder()
            .state(SUBMITTED)
            .respondents1(updatedRespondents)
            .build();

        when(orgApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .thenReturn(uk.gov.hmcts.reform.rd.model.Organisation.builder().name(ORGANISATION_NAME).build());

        postSubmittedEvent(toCallBackRequest(caseData, nocCaseDataBefore));

        final List<Map<String, ChangeOrganisationRequest>> update = Stream.of(expectedChange1, expectedChange2)
            .map(changeRequest -> Map.of("changeOrganisationRequestField", changeRequest))
            .collect(toList());

        verify(coreCaseDataApi, times(2)).startEventForCaseWorker(
            USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, SYS_USER_ID, JURISDICTION,
            CASE_TYPE, caseData.getId().toString(), "updateRepresentation"
        );

        final ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(coreCaseDataApi, times(2)).submitEventForCaseWorker(
            eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(SYS_USER_ID), eq(JURISDICTION),
            eq(CASE_TYPE), eq(caseData.getId().toString()), eq(true), captor.capture()
        );

        assertThat(captor.getAllValues())
            .extracting(CaseDataContent::getData)
            .containsExactlyInAnyOrderElementsOf(update);
    }

    @Test
    void shouldNotUpdateRepresentativesAccessWhenCaseNotSubmitted() {
        final CaseData caseData = nocCaseDataBefore.toBuilder()
            .state(OPEN)
            .respondents1(updatedRespondents)
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, nocCaseDataBefore));

        verifyNoInteractions(coreCaseDataApi);
    }

    private static Element<Respondent> respondent(Organisation organisation) {
        return element(Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .organisation(organisation)
                .build())
            .build());
    }

    private static Element<Respondent> respondent(Element<Respondent> respondent, Organisation organisation) {
        return element(respondent.getId(), respondent.getValue().toBuilder()
            .solicitor(RespondentSolicitor.builder()
                .organisation(organisation)
                .build())
            .build());
    }

    private Respondent respondent(LocalDate dateOfBirth) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .build();
    }
}
