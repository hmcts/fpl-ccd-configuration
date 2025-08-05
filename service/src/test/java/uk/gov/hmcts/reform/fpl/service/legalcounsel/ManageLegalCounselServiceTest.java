package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorEvent;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.BARRISTER;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CHILDSOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITORC;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.getCaseConverterInstance;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondent;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class ManageLegalCounselServiceTest {

    private static final String BARRISTER_EMAIL = "ted.robinson@example.com";
    private static final Element<LegalCounsellor> TEST_LEGAL_COUNSELLOR = element(LegalCounsellor.builder()
        .firstName("Ted")
        .lastName("Robinson")
        .email(BARRISTER_EMAIL)
        .organisation(Organisation.organisation("123"))
        .build()
    );
    private static final String USER_ID = "user_id";
    private static final Element<LegalCounsellor> UPDATED_LEGAL_COUNSELLOR = element(
        TEST_LEGAL_COUNSELLOR.getId(), TEST_LEGAL_COUNSELLOR.getValue().toBuilder().userId(USER_ID).build()
    );
    private static final String UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE = "Unable to grant access "
        + "[%s is not a Registered User] - "
        + "Email address for Counsel/External solicitor is not registered on the system. "
        + "They can register at https://manage-org.platform.hmcts.net/register-org/register";

    private final CaseConverter caseConverter = getCaseConverterInstance();
    private final OrganisationService organisationService = mock(OrganisationService.class);
    private final CaseAccessService caseAccessService = mock(CaseAccessService.class);
    private final CaseRoleLookupService caseRoleLookupService = mock(CaseRoleLookupService.class);
    private final UserService userService = mock(UserService.class);
    private final ManageLegalCounselService underTest =
        new ManageLegalCounselService(caseConverter, caseRoleLookupService, organisationService, caseAccessService,
                userService);

    @Nested
    class SolicitorTests {
        @BeforeEach
        void setUp() {
            when(caseAccessService.getUserCaseRoles(TEST_CASE_ID))
                    .thenReturn(Set.of(SOLICITORB, SOLICITORC, CHILDSOLICITORA, CHILDSOLICITORC));
            when(caseRoleLookupService.getCaseSolicitorRolesByCaseRoles(any()))
                    .thenReturn(List.of(SolicitorRole.SOLICITORB, SolicitorRole.SOLICITORC,
                            SolicitorRole.CHILDSOLICITORA, SolicitorRole.CHILDSOLICITORC));
        }

        @Test
        void shouldRetrieveNoLegalCounselForSolicitorUserWithNoLegalCounsel() {
            List<Element<LegalCounsellor>> retrievedLegalCounsel =
                    underTest.retrieveLegalCounselForLoggedInSolicitor(buildCaseData());

            assertThat(retrievedLegalCounsel).isEmpty();
        }

        @Test
        void shouldRetrieveExistingLegalCounselForSolicitorUserWithExistingLegalCounsel() {
            List<Element<LegalCounsellor>> legalCounsellors = List.of(TEST_LEGAL_COUNSELLOR);
            CaseData caseData = buildCaseData();
            caseData.getAllRespondents().get(SolicitorRole.SOLICITORB.getIndex()).getValue()
                    .setLegalCounsellors(legalCounsellors);

            List<Element<LegalCounsellor>> retrievedLegalCounsel =
                    underTest.retrieveLegalCounselForLoggedInSolicitor(caseData);

            assertThat(retrievedLegalCounsel).isEqualTo(legalCounsellors);
        }

        @Test
        void shouldUpdateLegalCounselInCaseDataAndResetEventData() {
            CaseData caseData = buildCaseData();
            CaseDetails caseDetails = CaseDetails.builder()
                    .id(TEST_CASE_ID)
                    .data(caseConverter.toMap(caseData))
                    .build();

            when(organisationService.findUserByEmail(TEST_LEGAL_COUNSELLOR.getValue().getEmail()))
                    .thenReturn(Optional.of(USER_ID));

            underTest.updateLegalCounsel(caseDetails);

            CaseData convertedCaseData = caseConverter.convert(caseDetails);
            assertThat(convertedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();

            List<Element<Child>> allChildren = convertedCaseData.getAllChildren();
            assertThat(allChildren.get(0).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(UPDATED_LEGAL_COUNSELLOR);
            assertThat(allChildren.get(1).getValue().getLegalCounsellors()).isNull();
            assertThat(allChildren.get(2).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(UPDATED_LEGAL_COUNSELLOR);

            List<Element<Respondent>> allRespondents = convertedCaseData.getAllRespondents();
            assertThat(allRespondents.get(0).getValue().getLegalCounsellors()).isNull();
            assertThat(allRespondents.get(1).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(UPDATED_LEGAL_COUNSELLOR);
            assertThat(allRespondents.get(2).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(UPDATED_LEGAL_COUNSELLOR);
        }
    }

    @Nested
    class BarristerTests {
        @BeforeEach
        void setUp() {
            when(caseAccessService.getUserCaseRoles(TEST_CASE_ID)).thenReturn(Set.of(BARRISTER));
            when(userService.getUserEmail()).thenReturn(BARRISTER_EMAIL.toUpperCase());
        }

        @Test
        void shouldRetrieveNoLegalCounselIfNoLegalCounsel() {
            List<Element<LegalCounsellor>> retrievedLegalCounsel =
                    underTest.retrieveLegalCounselForLoggedInSolicitor(buildCaseData());

            assertThat(retrievedLegalCounsel).isEmpty();
        }

        @Test
        void shouldRetrieveExistingLegalCounselWithSameEmailAsLoggedInUser() {
            List<Element<LegalCounsellor>> legalCounsellors = List.of(TEST_LEGAL_COUNSELLOR);
            CaseData caseData = buildCaseData();
            caseData.getAllRespondents().get(SolicitorRole.SOLICITORB.getIndex()).getValue()
                    .setLegalCounsellors(legalCounsellors);

            List<Element<LegalCounsellor>> retrievedLegalCounsel =
                    underTest.retrieveLegalCounselForLoggedInSolicitor(caseData);

            assertThat(retrievedLegalCounsel).isEqualTo(legalCounsellors);
        }

        @Test
        void shouldUpdateLegalCounselInCaseDataAndResetEventData() {
            Element<LegalCounsellor> eventUpdatedLegalCounsellor = element(TEST_LEGAL_COUNSELLOR.getId(),
                    TEST_LEGAL_COUNSELLOR.getValue().toBuilder().lastName("updatedLastName").build());
            CaseData caseData = buildCaseData().toBuilder()
                    .children1(wrapElements(
                            Child.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build(),
                            Child.builder().build(),
                            Child.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build()))
                    .respondents1(wrapElements(
                            Respondent.builder().build(),
                            Respondent.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build(),
                            Respondent.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build()))
                    .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                            .legalCounsellors(List.of(eventUpdatedLegalCounsellor)).build())
                    .build();
            CaseDetails caseDetails = CaseDetails.builder()
                    .id(TEST_CASE_ID)
                    .data(caseConverter.toMap(caseData))
                    .build();

            when(organisationService.findUserByEmail(TEST_LEGAL_COUNSELLOR.getValue().getEmail()))
                    .thenReturn(Optional.of(USER_ID));

            underTest.updateLegalCounsel(caseDetails);

            CaseData convertedCaseData = caseConverter.convert(caseDetails);
            assertThat(convertedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();


            Element<LegalCounsellor> expectedUpdatedLegalCounsellor = element(eventUpdatedLegalCounsellor.getId(),
                    eventUpdatedLegalCounsellor.getValue().toBuilder().lastName("updatedLastName").userId(USER_ID)
                            .build());

            List<Element<Child>> allChildren = convertedCaseData.getAllChildren();
            assertThat(allChildren.get(0).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(expectedUpdatedLegalCounsellor);
            assertThat(allChildren.get(1).getValue().getLegalCounsellors()).isNull();
            assertThat(allChildren.get(2).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(expectedUpdatedLegalCounsellor);

            List<Element<Respondent>> allRespondents = convertedCaseData.getAllRespondents();
            assertThat(allRespondents.get(0).getValue().getLegalCounsellors()).isNull();
            assertThat(allRespondents.get(1).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(expectedUpdatedLegalCounsellor);
            assertThat(allRespondents.get(2).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(expectedUpdatedLegalCounsellor);
        }

        @Test
        void shouldDeleteSelfFromCaseData() {
            Element<LegalCounsellor> legalCounsellor2 = element(TEST_LEGAL_COUNSELLOR.getValue().toBuilder()
                    .email("otherCounsellor@test.com").userId(USER_ID).build());
            CaseData caseData = buildCaseData().toBuilder()
                    .children1(wrapElements(
                            Child.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build(),
                            Child.builder().build(),
                            Child.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR, legalCounsellor2))
                                    .build()))
                    .respondents1(wrapElements(
                            Respondent.builder().build(),
                            Respondent.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR, legalCounsellor2))
                                    .build(),
                            Respondent.builder().legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build()))
                    .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                            .legalCounsellors(List.of()).build())
                    .build();
            CaseDetails caseDetails = CaseDetails.builder()
                    .id(TEST_CASE_ID)
                    .data(caseConverter.toMap(caseData))
                    .build();

            when(organisationService.findUserByEmail(BARRISTER_EMAIL)).thenReturn(Optional.of(USER_ID));

            underTest.updateLegalCounsel(caseDetails);

            CaseData convertedCaseData = caseConverter.convert(caseDetails);
            assertThat(convertedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();

            List<Element<Child>> allChildren = convertedCaseData.getAllChildren();
            assertThat(allChildren.get(0).getValue().getLegalCounsellors()).isNull();
            assertThat(allChildren.get(1).getValue().getLegalCounsellors()).isNull();
            assertThat(allChildren.get(2).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(legalCounsellor2);

            List<Element<Respondent>> allRespondents = convertedCaseData.getAllRespondents();
            assertThat(allRespondents.get(0).getValue().getLegalCounsellors()).isNull();
            assertThat(allRespondents.get(1).getValue().getLegalCounsellors())
                    .hasSize(1)
                    .contains(legalCounsellor2);
            assertThat(allRespondents.get(2).getValue().getLegalCounsellors()).isNull();
        }

        @Test
        void shouReturnErrorMessageIfNewEntryAdded() {
            CaseData caseData = buildCaseData().toBuilder()
                .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                    .legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR, TEST_LEGAL_COUNSELLOR)).build())
                .build();

            when(organisationService.findUserByEmail(TEST_LEGAL_COUNSELLOR.getValue().getEmail()))
                    .thenReturn(Optional.of(USER_ID));
            List<String> errorMessages = underTest.validateEventData(caseData);

            assertThat(errorMessages).hasSize(1).contains("Unable to add new legal counsellor. "
                    + "If you wish to add a new legal counsellor, please contact the the corresponding solicitors.");
        }

        @Test
        void shouReturnErrorMessageIfEmailAddressUpdated() {
            Element<LegalCounsellor> updatedLegalCounsellor = element(
                    TEST_LEGAL_COUNSELLOR.getValue().toBuilder().email("updated@test.com").build());

            CaseData caseData = buildCaseData().toBuilder()
                    .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                            .legalCounsellors(List.of(updatedLegalCounsellor)).build())
                    .build();

            when(organisationService.findUserByEmail(TEST_LEGAL_COUNSELLOR.getValue().getEmail()))
                    .thenReturn(Optional.of(USER_ID));
            when(organisationService.findUserByEmail("updated@test.com")).thenReturn(Optional.of(USER_ID));
            List<String> errorMessages = underTest.validateEventData(caseData);

            assertThat(errorMessages).hasSize(1).contains("The email address cannot be changed. "
                    + "If you wish to remove yourself from the case, please delete your entry.");
        }
    }

    @Test
    void shouldReturnErrorMessageForUnregisteredLegalCounsellors() {
        when(organisationService.findUserByEmail("ted.robinson@example.com")).thenReturn(Optional.empty());
        when(organisationService.findUserByEmail("john.johnson@example.com")).thenReturn(Optional.of("testUser2"));
        when(organisationService.findUserByEmail("peter.patrick@example.com")).thenReturn(Optional.empty());
        CaseData caseData = buildCaseData().toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(List.of(
                        TEST_LEGAL_COUNSELLOR,
                        element(LegalCounsellor.builder().firstName("John").lastName("Johnson")
                            .email("john.johnson@example.com")
                            .organisation(Organisation.organisation("Test organisation 1"))
                            .build()
                        ),
                        element(LegalCounsellor.builder().firstName("Peter").lastName("Patrick")
                            .email("peter.patrick@example.com")
                            .organisation(Organisation.organisation("Test organisation 2"))
                            .build()
                        )
                    ))
                    .build()
            )
            .build();

        List<String> errorMessages = underTest.validateEventData(caseData);

        assertThat(errorMessages).hasSize(2)
            .contains(format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, "Ted Robinson"))
            .contains(format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, "Peter Patrick"));
    }

    @Test
    void shouldReturnErrorMessageForLegalCounsellorsWithNoSelectedOrganisation() {
        when(organisationService.findUserByEmail("damian.king@example.com")).thenReturn(Optional.of("testUser1"));
        when(organisationService.findUserByEmail("john.johnson@example.com")).thenReturn(Optional.of("testUser2"));
        when(organisationService.findUserByEmail("peter.patrick@example.com")).thenReturn(Optional.of("testUser3"));
        CaseData caseData = buildCaseData().toBuilder()
            .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                .legalCounsellors(List.of(
                    element(LegalCounsellor.builder().firstName("Damian").lastName("King")
                        .email("damian.king@example.com")
                        .build()
                    ),
                    element(LegalCounsellor.builder().firstName("John").lastName("Johnson")
                        .email("john.johnson@example.com")
                        .organisation(Organisation.organisation("Test organisation 1"))
                        .build()
                    ),
                    element(LegalCounsellor.builder().firstName("Peter").lastName("Patrick")
                        .email("peter.patrick@example.com")
                        .organisation(Organisation.organisation("Test organisation 2"))
                        .build()
                    )
                ))
                .build()
            )
            .build();

        List<String> errorMessages = underTest.validateEventData(caseData);

        assertThat(errorMessages).hasSize(1).contains("Legal counsellor Damian King has no selected organisation");
    }

    @Test
    void shouldGrantAccessToNewLegalCounsellorsAndRevokeAccessFromRemovedLegalCounsellors() {
        when(caseAccessService.getUserCaseRoles(TEST_CASE_ID)).thenReturn(Set.of(SOLICITORB));
        when(caseRoleLookupService.getCaseSolicitorRolesByCaseRoles(any()))
                .thenReturn(List.of(SolicitorRole.SOLICITORB));
        when(organisationService.findOrganisation()).thenReturn(
            Optional.of(uk.gov.hmcts.reform.rd.model.Organisation.builder().name("Solicitors Law Ltd").build())
        );
        LegalCounsellor legalCounsellor1 = buildLegalCounsellor("1");
        LegalCounsellor legalCounsellor2 = buildLegalCounsellor("2");
        LegalCounsellor legalCounsellor3 = buildLegalCounsellor("3");

        CaseData previousCaseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .respondents1(wrapElements(
                respondent("First", "Respondent"),
                Respondent.builder()
                    .legalCounsellors(wrapElements(
                        legalCounsellor1, //Will be kept
                        legalCounsellor3 //Will be removed
                    ))
                    .build()
            ))
            .build();
        CaseData currentCaseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .respondents1(wrapElements(
                respondent("First", "Respondent"),
                Respondent.builder()
                    .legalCounsellors(wrapElements(
                        legalCounsellor1,//Existing
                        legalCounsellor2 //Added
                    ))
                    .build()
            ))
            .build();

        List<LegalCounsellorEvent> eventsToPublish = underTest.runFinalEventActions(previousCaseData, currentCaseData);

        assertThat(eventsToPublish)
            .hasSize(2)
            .containsExactly(
                new LegalCounsellorRemoved(currentCaseData, "Solicitors Law Ltd", legalCounsellor3),
                new LegalCounsellorAdded(currentCaseData, legalCounsellor2)
            );
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
                .id(TEST_CASE_ID)
                .children1(testChildren())
                .respondents1(respondents())
                .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                        .legalCounsellors(List.of(TEST_LEGAL_COUNSELLOR)).build())
                .build();
    }
}
