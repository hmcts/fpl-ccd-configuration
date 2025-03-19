package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_CHANGE_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(NoticeOfChangeController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfChangeControllerSubmittedTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 1234567890123456L;
    private static final String CASE_NAME = "Test";
    private static final String NEW_EMAIL = "new@test.com";
    private static final String OLD_EMAIL = "old@test.com";
    private static final String ORG_NAME = "org name";

    private static final RespondentParty RESPONDENT_PARTY = RespondentParty.builder()
        .firstName("John").lastName("Smith").build();

    private static final String OLD_ORG_ID = "123";
    private static final RespondentSolicitor OLD_REGISTERED_SOLICITOR = RespondentSolicitor.builder()
        .firstName("Old")
        .lastName("Solicitor")
        .email(OLD_EMAIL)
        .organisation(Organisation.builder().organisationID(OLD_ORG_ID).build())
        .build();
    private static final Element<Respondent> OLD_RESPONDENT = element(Respondent.builder()
        .party(RESPONDENT_PARTY)
        .solicitor(OLD_REGISTERED_SOLICITOR)
        .build());
    private static final String NEW_ORG_ID = "321";
    private static final RespondentSolicitor NEW_REGISTERED_SOLICITOR = RespondentSolicitor.builder()
        .firstName("New")
        .lastName("Solicitor")
        .email(NEW_EMAIL)
        .organisation(Organisation.builder().organisationID(NEW_ORG_ID).build())
        .build();
    private static final Element<Respondent> OTHER_RESPONDENT = element(Respondent.builder()
        .legalRepresentation("Yes")
        .solicitor(RespondentSolicitor.builder()
            .firstName("Other")
            .lastName("Solicitor")
            .email("other@test.com")
            .organisation(Organisation.builder().organisationID(OLD_ORG_ID).build()).build())
        .build());

    private static final List<Element<Child>> CHILDREN = wrapElements(Child.builder()
        .party(ChildParty.builder()
            .dateOfBirth(LocalDate.of(2015, 1, 1))
            .lastName("Jones").build())
        .build());

    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private OrganisationApi orgApi;

    NoticeOfChangeControllerSubmittedTest() {
        super("noc-decision");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
        givenSystemUser();

        when(orgApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, NEW_ORG_ID))
            .thenReturn(uk.gov.hmcts.reform.rd.model.Organisation.builder().name(ORG_NAME).build());
    }

    @Test
    void shouldRevokeAccessFromLegalCounsellors() throws NotificationClientException {
        String legalCounsellorEmail = "email";
        String legalCounsellorId = "id";
        LegalCounsellor counsellor = LegalCounsellor.builder()
            .firstName("Dave")
            .lastName("Watkins")
            .email(legalCounsellorEmail)
            .userId(legalCounsellorId)
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .respondents1(wrapElements(Respondent.builder()
                .solicitor(OLD_REGISTERED_SOLICITOR)
                .legalCounsellors(wrapElements(counsellor))
                .build()))
            .changeOrganisationRequestField(getChangeOrganisationRequest("[SOLICITORA]"))
            .children1(CHILDREN)
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(wrapElements(Respondent.builder().solicitor(NEW_REGISTERED_SOLICITOR).build()))
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
            "childLastName", "Jones",
            "clientFullName", ORG_NAME,
            "salutation", "Dear Dave Watkins"
        );

        verify(notificationClient).sendEmail(
            LEGAL_COUNSELLOR_REMOVED_EMAIL_TEMPLATE, legalCounsellorEmail, notifyData, "localhost/" + CASE_ID
        );

    }

    @Test
    void shouldNotifyRespondentSolicitorsWhenNoticeOfChangeIsSubmitted() {

        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeNewRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear New Solicitor");

        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeOldRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear Old Solicitor");

        final Map<String, Object> newSolicitorParameters = caseConverter.toMap(
            noticeOfChangeNewRespondentSolicitorTemplate);

        final Map<String, Object> oldSolicitorParameters = caseConverter.toMap(
            noticeOfChangeOldRespondentSolicitorTemplate);

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(CHILDREN)
            .respondents1(List.of(OTHER_RESPONDENT, OLD_RESPONDENT))
            .changeOrganisationRequestField(getChangeOrganisationRequest("[SOLICITORA]"))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(List.of(OTHER_RESPONDENT, element(
                OLD_RESPONDENT.getId(), Respondent.builder()
                    .legalRepresentation("Yes")
                    .party(RESPONDENT_PARTY)
                    .solicitor(NEW_REGISTERED_SOLICITOR)
                    .build())))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
                    NEW_EMAIL,
                    newSolicitorParameters,
                    notificationReference(CASE_ID));

                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    OLD_EMAIL,
                    oldSolicitorParameters,
                    notificationReference(CASE_ID));
            }
        );
    }

    @Test
    void shouldNotifyNewRespondentSolicitorWhenNoPreviousRepresentationAndNoticeOfChangeIsSubmitted() {
        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeNewRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParameters("Dear New Solicitor");

        final Map<String, Object> newSolicitorParameters = caseConverter.toMap(
            noticeOfChangeNewRespondentSolicitorTemplate);

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(CHILDREN)
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RESPONDENT_PARTY)
                    .legalRepresentation("No")
                    .build())
            )
            .changeOrganisationRequestField(getChangeOrganisationRequest("[SOLICITORA]"))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .legalRepresentation("Yes")
                    .party(RESPONDENT_PARTY)
                    .solicitor(NEW_REGISTERED_SOLICITOR)
                    .build()))
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> verify(notificationClient, times(1)).sendEmail(
            NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
            NEW_EMAIL,
            newSolicitorParameters,
            notificationReference(CASE_ID))
        );
    }

    @Test
    void shouldNotifyNewThirdPartyApplicantSolicitorsWhenNoticeOfChangeIsSubmitted() {
        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeNewRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParametersThirdPartyApplication(EMPTY);

        NoticeOfChangeRespondentSolicitorTemplate noticeOfChangeOldRespondentSolicitorTemplate =
            getExpectedNoticeOfChangeParametersThirdPartyApplication(EMPTY);

        final Map<String, Object> newSolicitorParameters = caseConverter.toMap(
            noticeOfChangeNewRespondentSolicitorTemplate);

        final Map<String, Object> oldSolicitorParameters = caseConverter.toMap(
            noticeOfChangeOldRespondentSolicitorTemplate);

        CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(CHILDREN)
            .localAuthorities(List.of(element(LocalAuthority.builder()
                .id(OLD_ORG_ID)
                .name("Joe Bloggs")
                .email(OLD_EMAIL)
                .phone("111222333")
                .address(Address.builder()
                    .addressLine1("Old Test Road")
                    .build())
                .build())))
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(OLD_ORG_ID).build())
                .build())
            .changeOrganisationRequestField(getChangeOrganisationRequest("[EPSMANAGING]"))
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorities(List.of(element(LocalAuthority.builder()
                .id(NEW_ORG_ID)
                .name("Joe Bloggs")
                .email(NEW_EMAIL)
                .phone("444555666")
                .address(Address.builder()
                    .addressLine1("New Test Road")
                    .build())
                .build())))
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(NEW_ORG_ID)
                    .build())
                .build())
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_NEW_REPRESENTATIVE,
                    NEW_EMAIL,
                    newSolicitorParameters,
                    notificationReference(CASE_ID));

                verify(notificationClient, times(1)).sendEmail(
                    NOTICE_OF_CHANGE_FORMER_REPRESENTATIVE,
                    OLD_EMAIL,
                    oldSolicitorParameters,
                    notificationReference(CASE_ID));
            }
        );
    }

    private NoticeOfChangeRespondentSolicitorTemplate getExpectedNoticeOfChangeParameters(String expectedSalutation) {
        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseUrl(caseUrl(CASE_ID))
            .clientFullName("John Smith")
            .childLastName("Jones")
            .build();
    }

    private NoticeOfChangeRespondentSolicitorTemplate getExpectedNoticeOfChangeParametersThirdPartyApplication(
        String expectedSalutation) {
        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseUrl(caseUrl(CASE_ID))
            .clientFullName(EMPTY)
            .childLastName("Jones")
            .build();
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(String caseRoleId) {
        return ChangeOrganisationRequest.builder()
            .organisationToAdd(organisation("NEW_ORG"))
            .caseRoleId(caseRoleDynamicList(caseRoleId))
            .build();

    }
}
