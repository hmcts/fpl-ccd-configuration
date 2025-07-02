package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorEvent;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORC;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.getCaseConverterInstance;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellor;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondent;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class ManageLegalCounselServiceTest {

    private static final Element<LegalCounsellor> TEST_LEGAL_COUNSELLOR = element(LegalCounsellor.builder()
        .firstName("Ted")
        .lastName("Robinson")
        .email("ted.robinson@example.com")
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
    private final CaseRoleLookupService caseRoleLookupService = mock(CaseRoleLookupService.class);
    private final OrganisationService organisationService = mock(OrganisationService.class);
    private final ManageLegalCounselService underTest =
        new ManageLegalCounselService(caseConverter, caseRoleLookupService, organisationService);

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        when(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID))
            .thenReturn(asList(SOLICITORB, SOLICITORC, CHILDSOLICITORA, CHILDSOLICITORC));

        caseData = CaseData.builder()
            .id(TEST_CASE_ID)
            .children1(testChildren())
            .respondents1(respondents())
            .build();
    }

    @Test
    void shouldRetrieveNoLegalCounselForSolicitorUserWithNoLegalCounsel() {
        List<Element<LegalCounsellor>> retrievedLegalCounsel =
            underTest.retrieveLegalCounselForLoggedInSolicitor(caseData);

        assertThat(retrievedLegalCounsel).isEmpty();
    }

    @Test
    void shouldRetrieveExistingLegalCounselForSolicitorUserWithExistingLegalCounsel() {
        List<Element<LegalCounsellor>> legalCounsellors = singletonList(TEST_LEGAL_COUNSELLOR);
        caseData.getAllRespondents().get(SOLICITORB.getIndex()).getValue().setLegalCounsellors(legalCounsellors);
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder().legalCounsellors(legalCounsellors).build()
            )
            .build();

        List<Element<LegalCounsellor>> retrievedLegalCounsel =
            underTest.retrieveLegalCounselForLoggedInSolicitor(caseData);

        assertThat(retrievedLegalCounsel).isEqualTo(legalCounsellors);
    }

    @Test
    void shouldUpdateLegalCounselInCaseDataAndResetEventData() {
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder().legalCounsellors(singletonList(TEST_LEGAL_COUNSELLOR)).build()
            )
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

    @Test
    void shouldReturnErrorMessageForUnregisteredLegalCounsellors() {
        when(organisationService.findUserByEmail("ted.robinson@example.com")).thenReturn(Optional.empty());
        when(organisationService.findUserByEmail("john.johnson@example.com")).thenReturn(Optional.of("testUser2"));
        when(organisationService.findUserByEmail("peter.patrick@example.com")).thenReturn(Optional.empty());
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(
                ManageLegalCounselEventData.builder()
                    .legalCounsellors(asList(
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
        caseData = caseData.toBuilder()
            .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
                .legalCounsellors(asList(
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

}
