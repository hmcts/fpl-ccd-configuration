package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_2;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER_COURT;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerAboutToSubmitTest extends AbstractCallbackTest {

    @Autowired
    private Time time;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private DfjAreaLookUpService dfjAreaLookUpService;

    @Captor
    private ArgumentCaptor<DecisionRequest> assignment;

    ManageLocalAuthoritiesControllerAboutToSubmitTest() {
        super("manage-local-authorities");
    }

    private final Organisation localAuthorityOrganisation = Organisation.builder()
        .organisationIdentifier(LOCAL_AUTHORITY_2_ID)
        .name(LOCAL_AUTHORITY_2_NAME)
        .contactInformation(List.of(ContactInformation.builder()
            .addressLine1("Line 1")
            .postCode("AB 100")
            .build()))
        .build();

    private final DfjAreaCourtMapping dfjAreaCourtMapping = DfjAreaCourtMapping.builder()
        .dfjArea("updatedDFJArea")
        .courtField("dfjCourt")
        .build();

    @BeforeEach
    void setup() {
        givenFplService();
        givenSystemUser();

        given(organisationApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, LOCAL_AUTHORITY_2_ID))
            .willReturn(localAuthorityOrganisation);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAddSecondaryLocalAuthority(Boolean isUserLaSolicitor) {

        final DynamicList localAuthorities = dynamicLists.from(0,
            Pair.of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
            Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
            .localAuthorityAction(isUserLaSolicitor ? null : ADD)
            .localAuthorityActionLA(isUserLaSolicitor ? ADD : null)
            .localAuthoritiesToShare(localAuthorities)
            .localAuthorityEmail("test@test.com")
            .build();

        final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_1_ID)
            .name(LOCAL_AUTHORITY_1_NAME)
            .designated("Yes")
            .build();

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(null)
            .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
            .localAuthoritiesEventData(eventData)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .build();

        final LocalAuthority expectedSecondaryLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_2_ID)
            .name(LOCAL_AUTHORITY_2_NAME)
            .email("test@test.com")
            .designated("No")
            .address(Address.builder()
                .addressLine1("Line 1")
                .postcode("AB 100")
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        final OrganisationPolicy expectedSharedOrganisationPolicy = organisationPolicy(LOCAL_AUTHORITY_2_ID,
            LOCAL_AUTHORITY_2_NAME, LASHARED);

        assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue)
            .containsExactly(designatedLocalAuthority, expectedSecondaryLocalAuthority);

        assertThat(updatedCaseData.getSharedLocalAuthorityPolicy()).isEqualTo(expectedSharedOrganisationPolicy);

        verifyNoInteractions(caseAssignmentApi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRemoveSecondaryLocalAuthority(Boolean isUserLaSolicitor) {

        final AboutToStartOrSubmitCallbackResponse nocResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(Map.of("key", "value"))
            .build();

        final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
            .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
            .localAuthorityAction(isUserLaSolicitor ? null : REMOVE)
            .localAuthorityActionLA(isUserLaSolicitor ? REMOVE : null)
            .localAuthorityAction(REMOVE)
            .localAuthorityToRemove(LOCAL_AUTHORITY_2_NAME)
            .build();

        final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_1_ID)
            .name(LOCAL_AUTHORITY_1_NAME)
            .designated("Yes")
            .build();

        final LocalAuthority secondaryLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_2_ID)
            .name(LOCAL_AUTHORITY_2_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .sharedLocalAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_2_ID, LOCAL_AUTHORITY_2_NAME, LASHARED))
            .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
            .localAuthoritiesEventData(eventData)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .build();

        final ChangeOrganisationRequest expectedChangeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(caseRoleDynamicList(LASHARED.formattedName()))
            .requestTimestamp(time.now())
            .approvalStatus(APPROVED)
            .organisationToRemove(caseData.getSharedLocalAuthorityPolicy().getOrganisation())
            .build();

        when(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), assignment.capture()))
            .thenReturn(nocResponse);

        final AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        final CaseData updatedCaseData = extractCaseData(assignment.getValue().getCaseDetails());

        assertThat(response).isEqualTo(nocResponse);

        assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue)
            .containsExactly(designatedLocalAuthority);

        assertThat(updatedCaseData.getChangeOrganisationRequestField()).isEqualTo(expectedChangeRequest);

        verifyNoInteractions(organisationApi);
    }

    @Nested
    class Transfer {

        private final LocalAuthority newDesignatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_2_ID)
            .name("New" + LOCAL_AUTHORITY_2_NAME)
            .email("New" + LOCAL_AUTHORITY_2_INBOX)
            .address(testAddress())
            .build();

        private final Colleague newDesignatedLocalAuthoritySolicitor = Colleague.builder()
            .role(SOLICITOR)
            .fullName("John Green")
            .email("john.green@test.com")
            .notificationRecipient("Yes")
            .build();

        private final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
            .id(LOCAL_AUTHORITY_1_ID)
            .name(LOCAL_AUTHORITY_1_NAME)
            .designated("Yes")
            .build();

        private final ChangeOrganisationRequest expectedChangeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(caseRoleDynamicList(LASOLICITOR.formattedName()))
            .requestTimestamp(time.now())
            .approvalStatus(APPROVED)
            .organisationToAdd(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(newDesignatedLocalAuthority.getId())
                .organisationName(newDesignatedLocalAuthority.getName())
                .build())
            .organisationToRemove(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID(designatedLocalAuthority.getId())
                .organisationName(designatedLocalAuthority.getName())
                .build())
            .build();

        @BeforeEach
        void init() {
            when(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), assignment.capture()))
                .thenAnswer(invocationOnMock -> AboutToStartOrSubmitCallbackResponse.builder()
                    .data(invocationOnMock.getArgument(2, DecisionRequest.class)
                        .getCaseDetails()
                        .getData())
                    .build());
        }

        @Test
        void shouldTransferCaseToSecondaryLocalAuthorityAndCourt() {

            when(dfjAreaLookUpService.getDfjArea(COURT_2.getCode()))
                .thenReturn(dfjAreaCourtMapping);

            final Colleague existingSocialWorker = Colleague.builder()
                .role(SOCIAL_WORKER)
                .fullName("Alex White")
                .email("alex.white@test.com")
                .notificationRecipient("Yes")
                .build();

            final Colleague existingSolicitor = Colleague.builder()
                .role(SOLICITOR)
                .dx("DX1")
                .reference("SOL1")
                .fullName("Emma Williams")
                .email("emma.williams@test.com")
                .phone("7777777777")
                .notificationRecipient("Yes")
                .build();

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .courtsToTransfer(dynamicLists.from(1,
                    Pair.of(COURT_1.getName(), COURT_1.getCode()),
                    Pair.of(COURT_2.getName(), COURT_2.getCode())))
                .localAuthorityToTransfer(newDesignatedLocalAuthority)
                .localAuthorityToTransferSolicitor(newDesignatedLocalAuthoritySolicitor)
                .build();

            final LocalAuthority secondaryLocalAuthority = LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_2_ID)
                .name(LOCAL_AUTHORITY_2_NAME)
                .email(LOCAL_AUTHORITY_2_INBOX)
                .address(testAddress())
                .pbaNumber("PBA1234567")
                .phone("7777777777")
                .colleagues(wrapElements(existingSocialWorker, existingSolicitor))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .court(COURT_1)
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .sharedLocalAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_2_ID, LOCAL_AUTHORITY_2_NAME, LASHARED))
                .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
                .build();

            final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

            final CaseData caseDataSentToAssignment = extractCaseData(assignment.getValue().getCaseDetails());
            assertThat(caseDataSentToAssignment.getChangeOrganisationRequestField()).isEqualTo(expectedChangeRequest);

            assertThat(updatedCaseData.getSharedLocalAuthorityPolicy()).isNull();
            assertThat(updatedCaseData.getCaseLocalAuthority()).isEqualTo(LOCAL_AUTHORITY_2_CODE);
            assertThat(updatedCaseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthority.getName());
            assertThat(updatedCaseData.getCourt()).isEqualTo(COURT_2);
            assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue).containsExactly(
                LocalAuthority.builder()
                    .id(newDesignatedLocalAuthority.getId())
                    .name(newDesignatedLocalAuthority.getName())
                    .email(newDesignatedLocalAuthority.getEmail())
                    .address(newDesignatedLocalAuthority.getAddress())
                    .pbaNumber(secondaryLocalAuthority.getPbaNumber())
                    .phone(secondaryLocalAuthority.getPhone())
                    .designated("Yes")
                    .colleagues(wrapElements(existingSocialWorker, Colleague.builder()
                        .role(newDesignatedLocalAuthoritySolicitor.getRole())
                        .fullName(newDesignatedLocalAuthoritySolicitor.getFullName())
                        .email(newDesignatedLocalAuthoritySolicitor.getEmail())
                        .notificationRecipient(newDesignatedLocalAuthoritySolicitor.getNotificationRecipient())
                        .phone(existingSolicitor.getPhone())
                        .dx(existingSolicitor.getDx())
                        .reference(existingSolicitor.getReference())
                        .build()))
                    .build());
        }

        @Test
        void shouldTransferCaseToNewLocalAuthorityAndCourt2() {
            when(dfjAreaLookUpService.getDfjArea(COURT_1.getCode()))
                .thenReturn(dfjAreaCourtMapping);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthorityToTransfer(newDesignatedLocalAuthority)
                .localAuthorityToTransferSolicitor(newDesignatedLocalAuthoritySolicitor)
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .localAuthorityPolicy(organisationPolicy(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1_NAME, LASOLICITOR))
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(designatedLocalAuthority))
                .court(COURT_1)
                .build();

            final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

            final CaseData caseDataSentToAssignment = extractCaseData(assignment.getValue().getCaseDetails());
            assertThat(caseDataSentToAssignment.getChangeOrganisationRequestField()).isEqualTo(expectedChangeRequest);

            assertThat(updatedCaseData.getCaseLocalAuthority()).isEqualTo(LOCAL_AUTHORITY_2_CODE);
            assertThat(updatedCaseData.getCaseLocalAuthorityName()).isEqualTo(newDesignatedLocalAuthority.getName());
            assertThat(updatedCaseData.getSharedLocalAuthorityPolicy()).isNull();
            assertThat(updatedCaseData.getCourt()).isEqualTo(initialCaseData.getCourt());
            assertThat(updatedCaseData.getLocalAuthorities()).extracting(Element::getValue).containsExactly(
                LocalAuthority.builder()
                    .id(LOCAL_AUTHORITY_2_ID)
                    .name(newDesignatedLocalAuthority.getName())
                    .email(newDesignatedLocalAuthority.getEmail())
                    .address(newDesignatedLocalAuthority.getAddress())
                    .designated("Yes")
                    .colleagues(wrapElements(Colleague.builder()
                        .role(newDesignatedLocalAuthoritySolicitor.getRole())
                        .fullName(newDesignatedLocalAuthoritySolicitor.getFullName())
                        .email(newDesignatedLocalAuthoritySolicitor.getEmail())
                        .notificationRecipient(newDesignatedLocalAuthoritySolicitor.getNotificationRecipient())
                        .mainContact("Yes")
                        .build()))
                    .build());
            assertThat(updatedCaseData.getDfjArea()).isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedCaseData.getCourtField()).isNull();
        }
    }

    @Nested
    class TransferToAnotherCourt {

        @Test
        void shouldTransferToOrdinaryCourt() {
            when(dfjAreaLookUpService.getDfjArea("384"))
                .thenReturn(dfjAreaCourtMapping);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .courtsToTransferWithoutTransferLA(dynamicLists.from(1,
                    Pair.of("Worcester", "380"),
                    Pair.of("Wrexham", "384")))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .court(COURT_1)
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse resp = postAboutToSubmitEvent(initialCaseData);
            final CaseData updatedCaseData = extractCaseData(resp);

            Court currentCourt = updatedCaseData.getCourt();
            assertThat(currentCourt.getCode()).isEqualTo("384");
            assertThat(currentCourt.getName()).isEqualTo("Family Court sitting at Wrexham");
            assertThat(currentCourt.getDateTransferred()).isNotNull();
            assertThat(updatedCaseData.getPastCourtList()).hasSize(1);

            Court lastCourt = unwrapElements(updatedCaseData.getPastCourtList())
                .stream()
                .sorted(Comparator.comparing(Court::getDateTransferred).reversed())
                .findFirst().orElse(null);
            assertThat(lastCourt).isNotNull();
            assertThat(lastCourt.getCode()).isEqualTo(COURT_1.getCode());
            assertThat(updatedCaseData.getDfjArea()).isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedCaseData.getCourtField()).isNull();
            assertThat(resp.getData()).extracting("caseManagementLocation")
                .extracting("baseLocation", "region").containsExactly("637145", "7");
        }

        @Test
        void shouldTransferToOrdinaryCourtAgain() {
            when(dfjAreaLookUpService.getDfjArea("384"))
                .thenReturn(dfjAreaCourtMapping);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .courtsToTransferWithoutTransferLA(dynamicLists.from(1,
                    Pair.of("Worcester", "380"),
                    Pair.of("Wrexham", "384")))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .court(
                    Court.builder()
                        .name("Family Court sitting at Swansea")
                        .code("344")
                        .dateTransferred(LocalDateTime.of(1997, Month.JULY, 1, 23, 59))
                        .build()
                )
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .localAuthoritiesEventData(eventData)
                .pastCourtList(List.of(element(
                    Court.builder()
                        .code("378")
                        .name("Family Court sitting at Wolverhampton")
                        .dateTransferred(LocalDateTime.of(1997, Month.JUNE, 30, 23, 59))
                        .build()
                )))
                .build();

            final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

            Court currentCourt = updatedCaseData.getCourt();
            assertThat(currentCourt.getCode()).isEqualTo("384");
            assertThat(currentCourt.getName()).isEqualTo("Family Court sitting at Wrexham");
            assertThat(currentCourt.getDateTransferred()).isNotNull();
            assertThat(updatedCaseData.getPastCourtList()).hasSize(2);

            Court lastCourt = unwrapElements(updatedCaseData.getPastCourtList())
                .stream()
                .sorted(Comparator.comparing(Court::getDateTransferred).reversed())
                .findFirst().orElse(null);
            assertThat(lastCourt).isNotNull();
            assertThat(lastCourt.getCode()).isEqualTo("344");
            assertThat(updatedCaseData.getDfjArea()).isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedCaseData.getCourtField()).isNull();
        }

        @Test
        void shouldTransferToRcjHighCourt() {
            when(dfjAreaLookUpService.getDfjArea(RCJ_HIGH_COURT_CODE))
                .thenReturn(dfjAreaCourtMapping);
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .courtsToTransferWithoutTransferLA(dynamicLists.from(1,
                    Pair.of("Worcester", "380"),
                    Pair.of(RCJ_HIGH_COURT_NAME, RCJ_HIGH_COURT_CODE)))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .court(Court.builder().name("Family Court sitting at Swansea").code("344").build())
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .localAuthoritiesEventData(eventData)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

            assertThat(updatedCaseData.getCourt().getCode()).isEqualTo(RCJ_HIGH_COURT_CODE);
            assertThat(updatedCaseData.getCourt().getName()).isEqualTo(RCJ_HIGH_COURT_NAME);
            assertThat(updatedCaseData.getCourt().getDateTransferred()).isNotNull();
            assertThat(updatedCaseData.getPastCourtList()).hasSize(1);
            assertThat(updatedCaseData.getSendToCtsc()).isEqualTo(YesNo.NO.getValue());

            Court lastCourt = unwrapElements(updatedCaseData.getPastCourtList())
                .stream()
                .sorted(Comparator.comparing(Court::getDateTransferred).reversed())
                .findFirst().orElse(null);

            assertThat(lastCourt).isNotNull();
            assertThat(lastCourt.getCode()).isEqualTo("344");
            assertThat(updatedCaseData.getDfjArea()).isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedCaseData.getCourtField()).isNull();
        }

        @Test
        void shouldTransferOutOfTheHighCourt() {
            when(dfjAreaLookUpService.getDfjArea("380"))
                .thenReturn(dfjAreaCourtMapping);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .courtsToTransferWithoutTransferLA(dynamicLists.from(0,
                    Pair.of("Worcester", "380"),
                    Pair.of(RCJ_HIGH_COURT_NAME, RCJ_HIGH_COURT_CODE)))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .court(Court.builder().name(RCJ_HIGH_COURT_NAME).code(RCJ_HIGH_COURT_CODE).build())
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
                .localAuthoritiesEventData(eventData)
                .sendToCtsc(YesNo.NO.getValue())
                .build();

            final CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

            assertThat(updatedCaseData.getCourt().getCode()).isEqualTo("380");
            assertThat(updatedCaseData.getCourt().getName()).isEqualTo("Family Court sitting at Worcester");
            assertThat(updatedCaseData.getCourt().getDateTransferred()).isNotNull();
            assertThat(updatedCaseData.getPastCourtList()).hasSize(1);
            assertThat(updatedCaseData.getSendToCtsc()).isEqualTo(YesNo.YES.getValue());

            Court lastCourt = unwrapElements(updatedCaseData.getPastCourtList())
                .stream()
                .sorted(Comparator.comparing(Court::getDateTransferred).reversed())
                .findFirst().orElse(null);
            assertThat(lastCourt).isNotNull();
            assertThat(lastCourt.getCode()).isEqualTo(RCJ_HIGH_COURT_CODE);
            assertThat(updatedCaseData.getDfjArea()).isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedCaseData.getCourtField()).isNull();
        }

    }
}
