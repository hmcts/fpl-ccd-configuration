package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorAdded;
import uk.gov.hmcts.reform.fpl.events.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageLegalCounselEventData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORC;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.getCaseConverterInstance;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.LegalCounsellorTestHelper.buildLegalCounsellorAndMockUserId;
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
    private static final String UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE = "Unable to grant access "
        + "[%s is not a Registered User] - Email address for Legal representative is not registered on the system. "
        + "They can register at https://manage-org.platform.hmcts.net/register-org/register";

    private final CaseConverter caseConverter = getCaseConverterInstance();
    private final CaseRoleLookupService caseRoleLookupService = mock(CaseRoleLookupService.class);
    private final OrganisationService organisationService = mock(OrganisationService.class);
    private final EventService eventPublisher = mock(EventService.class);
    private final ManageLegalCounselService manageLegalCounselService =
        new ManageLegalCounselService(caseConverter, caseRoleLookupService, organisationService, eventPublisher);

    @BeforeEach
    void setUp() {
        when(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID))
            .thenReturn(asList(SOLICITORB, SOLICITORC, CHILDSOLICITORA, CHILDSOLICITORC));
    }

    @Test
    void shouldRetrieveNoLegalCounselForSolicitorUserWithNoLegalCounsel() {
        Map<String, Object> incomingCaseData = new HashMap<>() {
            {
                put("children1", testChildren());
                put("respondents1", respondents());
            }
        };
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_AS_LONG).data(incomingCaseData).build();

        List<Element<LegalCounsellor>> retrievedLegalCounsel =
            manageLegalCounselService.retrieveLegalCounselForLoggedInSolicitor(caseDetails);

        assertThat(retrievedLegalCounsel).isEmpty();
    }

    @Test
    void shouldRetrieveExistingLegalCounselForSolicitorUserWithExistingLegalCounsel() {
        List<Element<LegalCounsellor>> legalCounsellors = singletonList(TEST_LEGAL_COUNSELLOR);
        List<Element<Respondent>> respondents = respondents();
        respondents.get(SOLICITORB.getIndex()).getValue().setLegalCounsellors(legalCounsellors);
        Map<String, Object> incomingCaseData = new HashMap<>() {
            {
                put("children1", testChildren());
                put("respondents1", respondents);
                put("legalCounsellors", legalCounsellors);
            }
        };
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_AS_LONG).data(incomingCaseData).build();

        List<Element<LegalCounsellor>> retrievedLegalCounsel =
            manageLegalCounselService.retrieveLegalCounselForLoggedInSolicitor(caseDetails);

        assertThat(retrievedLegalCounsel).isEqualTo(legalCounsellors);
    }

    @Test
    void shouldUpdateLegalCounselInCaseDataAndResetEventData() {
        Map<String, Object> incomingCaseData = new HashMap<>() {
            {
                put("children1", testChildren());
                put("respondents1", respondents());
                put("legalCounsellors", singletonList(TEST_LEGAL_COUNSELLOR));
            }
        };
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_AS_LONG).data(incomingCaseData).build();

        manageLegalCounselService.updateLegalCounsel(caseDetails);

        CaseData convertedCaseData = caseConverter.convert(caseDetails);
        assertThat(convertedCaseData.getManageLegalCounselEventData().getLegalCounsellors()).isNull();

        List<Element<Child>> allChildren = convertedCaseData.getAllChildren();
        assertThat(allChildren.get(0).getValue().getLegalCounsellors())
            .hasSize(1)
            .contains(TEST_LEGAL_COUNSELLOR);
        assertThat(allChildren.get(1).getValue().getLegalCounsellors()).isNull();
        assertThat(allChildren.get(2).getValue().getLegalCounsellors())
            .hasSize(1)
            .contains(TEST_LEGAL_COUNSELLOR);

        List<Element<Respondent>> allRespondents = convertedCaseData.getAllRespondents();
        assertThat(allRespondents.get(0).getValue().getLegalCounsellors()).isNull();
        assertThat(allRespondents.get(1).getValue().getLegalCounsellors())
            .hasSize(1)
            .contains(TEST_LEGAL_COUNSELLOR);
        assertThat(allRespondents.get(2).getValue().getLegalCounsellors())
            .hasSize(1)
            .contains(TEST_LEGAL_COUNSELLOR);
    }

    @Test
    void shouldReturnErrorMessageForUnregisteredLegalCounsellors() {
        when(organisationService.findUserByEmail("ted.robinson@example.com")).thenReturn(Optional.empty());
        when(organisationService.findUserByEmail("john.johnson@example.com")).thenReturn(Optional.of("testUser2"));
        when(organisationService.findUserByEmail("peter.patrick@example.com")).thenReturn(Optional.empty());
        CaseData incomingCaseData = CaseData.builder()
            .manageLegalCounselEventData(ManageLegalCounselEventData.builder()
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
                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_AS_LONG)
            .data(caseConverter.toMap(incomingCaseData))
            .build();

        List<String> errorMessages = manageLegalCounselService.validateEventData(caseDetails);

        assertThat(errorMessages).hasSize(2)
            .contains(format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, "Ted Robinson"))
            .contains(format(UNREGISTERED_USER_ERROR_MESSAGE_TEMPLATE, "Peter Patrick"));
    }

    @Test
    void shouldReturnErrorMessageForLegalCounsellorsWithNoSelectedOrganisation() {
        when(organisationService.findUserByEmail("damian.king@example.com")).thenReturn(Optional.of("testUser1"));
        when(organisationService.findUserByEmail("john.johnson@example.com")).thenReturn(Optional.of("testUser2"));
        when(organisationService.findUserByEmail("peter.patrick@example.com")).thenReturn(Optional.of("testUser3"));
        CaseData incomingCaseData = CaseData.builder()
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
                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_AS_LONG)
            .data(caseConverter.toMap(incomingCaseData))
            .build();

        List<String> errorMessages = manageLegalCounselService.validateEventData(caseDetails);

        assertThat(errorMessages).hasSize(1).contains("Legal counsellor Damian King has no selected organisation");
    }

    @Test
    void shouldGrantAccessToNewLegalCounsellorsAndRevokeAccessFromRemovedLegalCounsellors() {
        when(organisationService.findOrganisation()).thenReturn(
            Optional.of(uk.gov.hmcts.reform.rd.model.Organisation.builder().name("Solicitors Law Ltd").build())
        );
        Pair<String, LegalCounsellor> legalCounsellor1 = buildLegalCounsellorAndMockUserId(organisationService, "1");
        Pair<String, LegalCounsellor> legalCounsellor2 = buildLegalCounsellorAndMockUserId(organisationService, "2");
        Pair<String, LegalCounsellor> legalCounsellor3 = buildLegalCounsellorAndMockUserId(organisationService, "3");
        CaseDetails previousCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .data(caseConverter.toMap(
                CaseData.builder().respondents1(asList(
                    element(respondent("First", "Respondent")),
                    element(Respondent.builder().legalCounsellors(asList(
                        element(legalCounsellor1.getValue()),//Will be kept
                        element(legalCounsellor3.getValue())//Will be removed
                    )).build())
                )).build()
            )).build();
        CaseDetails currentCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .data(caseConverter.toMap(
                CaseData.builder().respondents1(asList(
                    element(respondent("First", "Respondent")),
                    element(Respondent.builder().legalCounsellors(asList(
                        element(legalCounsellor1.getValue()),//Existing
                        element(legalCounsellor2.getValue())//Added
                    )).build())
                )).build()
            )).build();

        manageLegalCounselService.runFinalEventActions(previousCaseDetails, currentCaseDetails);

        CaseData currentCaseData = caseConverter.convert(currentCaseDetails);
        verify(eventPublisher).publishEvent(
            new LegalCounsellorAdded(currentCaseData, legalCounsellor2)
        );
        verify(eventPublisher).publishEvent(
            new LegalCounsellorRemoved(currentCaseData, "Solicitors Law Ltd", legalCounsellor3)
        );
        verifyNoMoreInteractions(eventPublisher);
    }

}
