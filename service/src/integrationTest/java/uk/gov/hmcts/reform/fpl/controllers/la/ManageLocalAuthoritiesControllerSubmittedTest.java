package uk.gov.hmcts.reform.fpl.controllers.la;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.model.notify.CaseTransferredNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedLocalAuthorityChangedNotifyData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_SOLICITOR_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildParty;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerSubmittedTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 1234L;
    private static final String CASE_NAME = "Case name";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ManageLocalAuthoritiesControllerSubmittedTest() {
        super("manage-local-authorities");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        givenSystemUser();
    }

    private final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
        .id(LOCAL_AUTHORITY_1_ID)
        .name(LOCAL_AUTHORITY_1_NAME)
        .email(LOCAL_AUTHORITY_1_INBOX)
        .designated("Yes")
        .build();

    private final LocalAuthority secondaryLocalAuthority = LocalAuthority.builder()
        .id(LOCAL_AUTHORITY_2_ID)
        .name(LOCAL_AUTHORITY_2_NAME)
        .email(LOCAL_AUTHORITY_2_INBOX)
        .designated("No")
        .build();

    private final OrganisationPolicy designatedOrganisationPolicy = organisationPolicy(LOCAL_AUTHORITY_1_ID,
        LOCAL_AUTHORITY_1_NAME, LASOLICITOR);

    private final OrganisationPolicy secondaryOrganisationPolicy = organisationPolicy(LOCAL_AUTHORITY_2_ID,
        LOCAL_AUTHORITY_2_NAME, LASHARED);

    @Test
    void shouldNotifyLocalAuthoritiesWhenSecondaryLocalAuthorityAdded() {

        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .sharedLocalAuthorityPolicy(null)
            .localAuthorityPolicy(designatedOrganisationPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .children1(List.of(testChild("Gregory", "White", BOY, LocalDate.now().minusYears(2))))
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .build();

        final SharedLocalAuthorityChangedNotifyData secondaryLocalAuthorityData = SharedLocalAuthorityChangedNotifyData
            .builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .secondaryLocalAuthority(LOCAL_AUTHORITY_2_NAME)
            .designatedLocalAuthority(LOCAL_AUTHORITY_1_NAME)
            .build();

        final SharedLocalAuthorityChangedNotifyData designatedLocalAuthorityData = secondaryLocalAuthorityData
            .toBuilder()
            .childLastName("White")
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE,
                LOCAL_AUTHORITY_1_INBOX,
                toMap(designatedLocalAuthorityData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE,
                LOCAL_AUTHORITY_2_INBOX,
                toMap(secondaryLocalAuthorityData),
                notificationReference(CASE_ID));
        });

        verifyNoMoreInteractions(notificationClient, coreCaseDataService);
    }

    @Test
    void shouldNotifyLocalAuthoritiesWhenSecondaryLocalAuthorityRemoved() {

        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .sharedLocalAuthorityPolicy(secondaryOrganisationPolicy)
            .localAuthorityPolicy(designatedOrganisationPolicy)
            .localAuthorities(wrapElements(
                designatedLocalAuthority,
                secondaryLocalAuthority.toBuilder().email(LOCAL_AUTHORITY_2_USER_EMAIL).build()))
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .sharedLocalAuthorityPolicy(null)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .build();

        final SharedLocalAuthorityChangedNotifyData secondaryLocalAuthorityData = SharedLocalAuthorityChangedNotifyData
            .builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .secondaryLocalAuthority(LOCAL_AUTHORITY_2_NAME)
            .designatedLocalAuthority(LOCAL_AUTHORITY_1_NAME)
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE,
                LOCAL_AUTHORITY_2_INBOX,
                toMap(secondaryLocalAuthorityData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE,
                LOCAL_AUTHORITY_2_USER_EMAIL,
                toMap(secondaryLocalAuthorityData),
                notificationReference(CASE_ID));
        });

        verifyNoMoreInteractions(notificationClient, coreCaseDataService);
    }

    @Test
    void shouldNotifyPreviousAndNewDesignatedLocalAuthorityAboutCaseTransfer() {

        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .children1(List.of(testChild("Alex", "Green", BOY, now().toLocalDate())))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorityPolicy(designatedOrganisationPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .localAuthoritiesEventData(LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .build())
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
            .localAuthorityPolicy(secondaryOrganisationPolicy)
            .localAuthorities(wrapElements(
                secondaryLocalAuthority.toBuilder().email(LOCAL_AUTHORITY_2_USER_EMAIL).designated("Yes").build()))
            .build();

        final CaseTransferredNotifyData notifyData = CaseTransferredNotifyData
            .builder()
            .caseName(caseData.getCaseName())
            .ccdNumber(caseData.getId().toString())
            .childLastName("Green")
            .caseUrl(caseUrl(CASE_ID))
            .prevDesignatedLocalAuthority(LOCAL_AUTHORITY_1_NAME)
            .newDesignatedLocalAuthority(LOCAL_AUTHORITY_2_NAME)
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE,
                LOCAL_AUTHORITY_2_USER_EMAIL,
                toMap(notifyData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE,
                LOCAL_AUTHORITY_1_INBOX,
                toMap(notifyData),
                notificationReference(CASE_ID));
        });

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());

        verifyNoMoreInteractions(notificationClient, coreCaseDataService);
    }

    void notifyAllPartiesWhenCaseTransferredToAnotherCourtTemplate(boolean isTransferredToRcjHighCourt) {
        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID).sendToCtsc(YES.getValue())
            .caseName("Case name")
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .children1(List.of(element(
                Child.builder()
                    .party(testChildParty("Gregory", "White", BOY, LocalDate.now().minusYears(2)))
                    .solicitor(RespondentSolicitor.builder()
                        .email("child_solicitor@solicitor.com")
                        .organisation(Organisation.builder().organisationID("TEST").build())
                        .build())
                    .build()
            )))
            .respondents1(List.of(element(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("John")
                        .lastName("Tang")
                        .build())
                    .solicitor(RespondentSolicitor.builder()
                        .email(PRIVATE_SOLICITOR_USER_EMAIL)
                        .organisation(Organisation.builder().organisationID("TEST").build())
                        .build())
                    .build()
            )))
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .court(
                Court.builder()
                    .code(isTransferredToRcjHighCourt ? RCJ_HIGH_COURT_CODE : "386")
                    .name(isTransferredToRcjHighCourt ? RCJ_HIGH_COURT_NAME : "York")
                    .region(isTransferredToRcjHighCourt ? RCJ_HIGH_COURT_REGION : "North East")
                    .dateTransferred(LocalDateTime.of(1997, Month.JULY, 1, 23, 59))
                    .build()
            )
            .pastCourtList(
                List.of(element(
                    Court.builder()
                        .code("118").name("Barnsley").region("North East")
                        .build()
                ))
            )
            .build();

        final SharedLocalAuthorityChangedNotifyData expectedNotifyData = SharedLocalAuthorityChangedNotifyData
            .builder()
            .courtName(isTransferredToRcjHighCourt ? RCJ_HIGH_COURT_NAME : "York")
            .previousCourtName("Barnsley")
            .ccdNumber(caseData.getId().toString())
            .caseName(caseData.getCaseName())
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                LOCAL_AUTHORITY_1_INBOX,
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                LOCAL_AUTHORITY_2_INBOX,
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                PRIVATE_SOLICITOR_USER_EMAIL,
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                "child_solicitor@solicitor.com",
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));
            // notify ctsc admin
            verify(notificationClient).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                "FamilyPublicLaw+ctsc@gmail.com",
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));
            // notify high court admin
            verify(notificationClient, times(isTransferredToRcjHighCourt ? 1 : 0)).sendEmail(
                CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE,
                "FamilyPublicLaw+rcjfamilyhighcourt@gmail.com",
                toMap(expectedNotifyData),
                notificationReference(CASE_ID));

        });

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());
        verifyNoMoreInteractions(notificationClient);
    }

    @Test
    void shouldNotifyAllPartiesWhenCaseTransferredToOrdinaryCourt() {
        notifyAllPartiesWhenCaseTransferredToAnotherCourtTemplate(false);
    }

    @Test
    void shouldNotifyAllPartiesWhenCaseTransferredToRcjHighCourt() {
        notifyAllPartiesWhenCaseTransferredToAnotherCourtTemplate(true);
    }

}
