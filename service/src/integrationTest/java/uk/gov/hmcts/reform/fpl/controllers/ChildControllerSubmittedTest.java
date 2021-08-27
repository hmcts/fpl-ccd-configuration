package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerSubmittedTest extends AbstractCallbackTest {

    private static final State NON_RESTRICTED_STATE = SUBMITTED;

    private static final String ORGANISATION_NAME = "Test organisation";
    private static final String ORGANISATION_ID = "dun dun duuuuuuuun *synthy*";
    private static final String MAIN_SOLICITOR_FIRST_NAME = "dun dun duuuuuuuun *orchestral*";
    private static final String MAIN_SOLICITOR_LAST_NAME = "dun dun duuuuuuuun *orchestral* x3";
    private static final String MAIN_SOLICITOR_EMAIL = "email";
    private static final Organisation MAIN_ORG = Organisation.builder()
        .organisationID(ORGANISATION_ID)
        .build();
    private static final RespondentSolicitor REGISTERED_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName(MAIN_SOLICITOR_FIRST_NAME)
        .lastName(MAIN_SOLICITOR_LAST_NAME)
        .email(MAIN_SOLICITOR_EMAIL)
        .organisation(MAIN_ORG)
        .build();
    private static final RespondentSolicitor UNREGISTERED_REPRESENTATIVE = RespondentSolicitor.builder()
        .firstName(MAIN_SOLICITOR_FIRST_NAME)
        .lastName(MAIN_SOLICITOR_LAST_NAME)
        .email(MAIN_SOLICITOR_EMAIL)
        .unregisteredOrganisation(UnregisteredOrganisation.builder().name(ORGANISATION_NAME).build())
        .build();
    private static final String CHILD_NAME_1 = "John";
    private static final String CHILD_SURNAME_1 = "Smith";
    private static final Long CASE_ID = 1234567890123456L;
    private static final String CASE_NAME = "case name";

    @SpyBean
    private EventService eventService;
    @SpyBean
    private NoticeOfChangeService nocService;

    @MockBean
    private CoreCaseDataService ccdService;
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private OrganisationApi orgApi;

    ChildControllerSubmittedTest() {
        super("enter-children");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
        givenSystemUser();
        when(orgApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
            .thenReturn(uk.gov.hmcts.reform.rd.model.Organisation.builder().name(ORGANISATION_NAME).build());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"OPEN", "RETURNED"})
    void doNothingWhenInOpenAndReturnedState(State state) {
        CaseData caseData = CaseData.builder()
            .state(state)
            .build();

        postSubmittedEvent(caseData);

        verifyNoInteractions(nocService, eventService);
    }

    @Test
    void shouldUpdateRepresentativeAccess() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .children1(wrapElements(Child.builder()
                .solicitor(REGISTERED_REPRESENTATIVE)
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(REGISTERED_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .children1(wrapElements(Child.builder()
                .solicitor(null)
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        Map<String, Object> changeRequest = Map.of(
            "changeOrganisationRequestField", ChangeOrganisationRequest.builder()
                .approvalStatus(APPROVED)
                .organisationToAdd(MAIN_ORG)
                .organisationToRemove(null)
                .caseRoleId(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .label(CHILDSOLICITORA.getCaseRoleLabel())
                        .code(CHILDSOLICITORA.getCaseRoleLabel())
                        .build())
                    .listItems(List.of(DynamicListElement.builder()
                        .label(CHILDSOLICITORA.getCaseRoleLabel())
                        .code(CHILDSOLICITORA.getCaseRoleLabel())
                        .build()))
                    .build())
                .requestTimestamp(now())
                .build()
        );

        verify(ccdService).triggerEvent(CASE_ID, "updateRepresentation", changeRequest);
    }

    @Test
    void shouldSendNotificationsToRegisteredRepresentatives() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .caseName(CASE_NAME)
            .children1(wrapElements(Child.builder()
                .solicitor(REGISTERED_REPRESENTATIVE)
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(REGISTERED_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                MAIN_SOLICITOR_EMAIL,
                Map.of(
                    "salutation", format("Dear %s %s", MAIN_SOLICITOR_FIRST_NAME, MAIN_SOLICITOR_LAST_NAME),
                    "clientFullName", format("%s %s", CHILD_NAME_1, CHILD_SURNAME_1),
                    "localAuthority", LOCAL_AUTHORITY_1_NAME,
                    "ccdNumber", CASE_ID.toString(),
                    "caseName", CASE_NAME,
                    "manageOrgLink", "https://manage-org.platform.hmcts.net",
                    "childLastName", CHILD_SURNAME_1
                ),
                "localhost/" + CASE_ID
            )
        );
    }

    @Test
    void shouldSendNotificationsToUnregisteredRepresentatives() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .caseName(CASE_NAME)
            .children1(wrapElements(Child.builder()
                .solicitor(UNREGISTERED_REPRESENTATIVE)
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(UNREGISTERED_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .state(NON_RESTRICTED_STATE)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder()
                    .firstName(CHILD_NAME_1)
                    .lastName(CHILD_SURNAME_1)
                    .dateOfBirth(dateNow())
                    .build())
                .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                MAIN_SOLICITOR_EMAIL,
                Map.of(
                    "ccdNumber", "1234-5678-9012-3456",
                    "localAuthority", LOCAL_AUTHORITY_1_NAME,
                    "clientFullName", format("%s %s", CHILD_NAME_1, CHILD_SURNAME_1),
                    "caseName", CASE_NAME,
                    "childLastName", CHILD_SURNAME_1
                ),
                "localhost/" + CASE_ID
            )
        );
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
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .children1(wrapElements(
                Child.builder()
                    .party(
                        ChildParty.builder()
                            .firstName(CHILD_NAME_1)
                            .lastName(CHILD_SURNAME_1)
                            .dateOfBirth(dateNow())
                            .build()
                    )
                    .solicitor(REGISTERED_REPRESENTATIVE)
                    .legalCounsellors(legalCounsellors)
                    .build()
            ))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .children1(wrapElements(
                Child.builder()
                    .party(
                        ChildParty.builder()
                            .firstName(CHILD_NAME_1)
                            .lastName(CHILD_SURNAME_1)
                            .dateOfBirth(dateNow())
                            .build()
                    )
                    .solicitor(UNREGISTERED_REPRESENTATIVE)
                    .build()
            ))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        CaseAssignedUserRolesRequest revokeRequestPayload = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                CaseAssignedUserRoleWithOrganisation.builder()
                    .userId(legalCounsellorId)
                    .caseRole("[BARRISTER]")
                    .caseDataId(CASE_ID.toString())
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
}
