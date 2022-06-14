package uk.gov.hmcts.reform.fpl.controllers.la;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ApplicantLocalAuthorityController;
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_3A;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_3B;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_SEPARATOR;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER_COURT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantLocalAuthorityController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLocalAuthoritiesControllerMidEventsTest extends AbstractCallbackTest {

    @MockBean
    private OrganisationApi organisationApi;

    ManageLocalAuthoritiesControllerMidEventsTest() {
        super("manage-local-authorities");
    }

    @Nested
    class ActionSelection {

        private final String callback = "action-selection";

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnErrorWhenUserTriesToRemoveNonExistingSecondaryLocalAuthority(Boolean isUserLaSolicitor) {

            final CaseData caseData = buildCaseData(isUserLaSolicitor, REMOVE, null);

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);

            assertThat(response.getErrors()).containsExactly(
                "There are no other local authorities to remove from this case");
        }

        private CaseData buildCaseData(Boolean isUserLaSolicitor,
                                       LocalAuthorityAction localAuthorityAction,
                                       OrganisationPolicy organisationPolicy) {
            return CaseData.builder()
                .sharedLocalAuthorityPolicy(organisationPolicy)
                .localAuthoritiesEventData(LocalAuthoritiesEventData.builder()
                    .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                    .localAuthorityAction(isUserLaSolicitor ? null : localAuthorityAction)
                    .localAuthorityActionLA(isUserLaSolicitor ? localAuthorityAction : null)
                    .build())
                .build();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnErrorWhenUserTriesToAddAnotherSharedLocalAuthority(Boolean isUserLaSolicitor) {

            final CaseData caseData = buildCaseData(
                isUserLaSolicitor, ADD, organisationPolicy("ORG", "ORG name", LASHARED));

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);

            assertThat(response.getErrors()).containsExactly(
                "Case access has already been given to local authority. Remove their access to continue.");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldAddNameOfLocalAuthorityToBeRemoved(Boolean isUserLaSolicitor) {

            final CaseData caseData = buildCaseData(
                isUserLaSolicitor, REMOVE, organisationPolicy("ORG", "ORG name", LASHARED));

            final CaseData updated = extractCaseData(postMidEvent(caseData, callback));

            final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                .localAuthorityAction(isUserLaSolicitor ? null : REMOVE)
                .localAuthorityActionLA(isUserLaSolicitor ? REMOVE : null)
                .localAuthorityToRemove("ORG name")
                .build();

            assertThat(updated.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldAddEmailOfLocalAuthorityToBeShared(Boolean isUserLaSolicitor) {

            final DynamicList localAuthorities = dynamicLists.from(1,
                Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
                Pair.of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
                Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                .localAuthorityAction(isUserLaSolicitor ? null : ADD)
                .localAuthorityActionLA(isUserLaSolicitor ? ADD : null)
                .localAuthoritiesToShare(localAuthorities)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final CaseData updated = extractCaseData(postMidEvent(caseData, callback));

            final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                .localAuthorityAction(isUserLaSolicitor ? null : ADD)
                .localAuthorityActionLA(isUserLaSolicitor ? ADD : null)
                .localAuthoritiesToShare(localAuthorities)
                .localAuthorityEmail(LOCAL_AUTHORITY_2_INBOX)
                .build();

            assertThat(updated.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }

        @Test
        void shouldAddLocalAuthoritiesToBeTransferred() {

            final DynamicList expectedDynamicList = dynamicLists.from(
                Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
                Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .build();

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
                .localAuthoritiesEventData(eventData)
                .build();

            final CaseData updated = extractCaseData(postMidEvent(caseData, callback));

            final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthoritiesToTransfer(expectedDynamicList)
                .localAuthoritiesToTransferWithoutShared(expectedDynamicList)
                .build();

            assertThat(updated.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }

        @Test
        void shouldReturnCurrentCourtNameAndCourtsListForSelection() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .build();

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
                .localAuthoritiesEventData(eventData)
                .build();

            final CaseData updated = extractCaseData(postMidEvent(caseData, callback));

            assertThat(updated.getLocalAuthoritiesEventData().getCurrentCourtNameWithoutTransferLA()).isNotBlank();
            assertThat(updated.getLocalAuthoritiesEventData().getCourtsToTransferWithoutTransferLA()).isNotNull();
            assertThat(updated.getLocalAuthoritiesEventData().getCourtsToTransferWithoutTransferLA().getListItems())
                .hasSize(75);
        }
    }

    @Nested
    class LocalAuthorityToAddDetails {

        private final String callback = "add/la-details";

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnValidateErrorsWhenProvidedEmailIsNotValid(Boolean isUserLaSolicitor) {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                .localAuthorityAction(isUserLaSolicitor ? null : ADD)
                .localAuthorityActionLA(isUserLaSolicitor ? ADD : null)
                .localAuthorityEmail("test")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);

            assertThat(response.getErrors())
                .containsExactly("Enter an email address in the correct format, for example name@example.com");

        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldNotReturnErrorsWhenProvidedEmailIsValid(Boolean isUserLaSolicitor) {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .isLaSolicitor(isUserLaSolicitor ? YesNo.YES : null)
                .localAuthorityAction(isUserLaSolicitor ? null : ADD)
                .localAuthorityActionLA(isUserLaSolicitor ? ADD : null)
                .localAuthorityEmail("test@test.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class LocalAuthorityToTransferSelection {

        private final String callback = "transfer/la-selection";

        @BeforeEach
        void setup() {
            givenFplService();
            givenSystemUser();
        }

        @Test
        void shouldPrepareDefaultLocalAuthorityToTransferDetials() {

            final DynamicList dynamicList = dynamicLists.from(0,
                Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
                Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

            final Organisation localAuthorityOrganisation = Organisation.builder()
                .organisationIdentifier(LOCAL_AUTHORITY_1_ID)
                .name(LOCAL_AUTHORITY_1_NAME)
                .contactInformation(List.of(ContactInformation.builder()
                    .addressLine1("Line 1")
                    .postCode("AB 100")
                    .build()))
                .build();

            given(organisationApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, LOCAL_AUTHORITY_1_ID))
                .willReturn(localAuthorityOrganisation);

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthoritiesToTransfer(dynamicList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, callback));

            final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthoritiesToTransfer(dynamicList)
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .id(LOCAL_AUTHORITY_1_ID)
                    .name(LOCAL_AUTHORITY_1_NAME)
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .address(Address.builder()
                        .addressLine1("Line 1")
                        .postcode("AB 100")
                        .build())
                    .colleagues(wrapElements(Colleague.builder()
                        .role(SOLICITOR)
                        .notificationRecipient("Yes")
                        .build()))
                    .build())
                .localAuthorityToTransferSolicitor(Colleague.builder()
                    .role(SOLICITOR)
                    .notificationRecipient("Yes")
                    .build())
                .build();

            assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }

        @Test
        void shouldTakeExistingLocalAuthorityToTransfer() {

            final DynamicList dynamicList = dynamicLists.from(0,
                Pair.of(LOCAL_AUTHORITY_1_NAME, LOCAL_AUTHORITY_1_CODE),
                Pair.of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE));

            final Colleague colleague1 = Colleague.builder()
                .role(OTHER)
                .title("Legal adviser")
                .fullName("Alex Brown")
                .email("alex.brown@test.com")
                .mainContact("Yes")
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague2 = Colleague.builder()
                .role(SOLICITOR)
                .fullName("John Smith")
                .email("john.smith@test.com")
                .notificationRecipient("Yes")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_2_ID)
                .name(LOCAL_AUTHORITY_2_NAME)
                .email(LOCAL_AUTHORITY_2_INBOX)
                .pbaNumber("PBA7654321")
                .phone("7654321")
                .designated("Yes")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_ID)
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .pbaNumber("PBA1234567")
                .phone("7777777")
                .colleagues(wrapElements(colleague1, colleague2))
                .build();

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthoritiesToTransfer(dynamicList)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, callback));

            final LocalAuthoritiesEventData expectedEventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthoritiesToTransfer(dynamicList)
                .localAuthorityToTransfer(localAuthority2.toBuilder()
                    .colleagues(wrapElements(colleague2))
                    .build())
                .localAuthorityToTransferSolicitor(colleague2)
                .build();

            assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }
    }

    @Nested
    class LocalAuthorityToTransferDetails {

        private final String callback = "transfer/la-details";

        @Test
        void shouldReturnValidateErrorsWhenProvidedEmailsAreNotValid() {

            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .email("test1")
                    .build())
                .localAuthorityToTransferSolicitor(Colleague.builder()
                    .email("test2")
                    .build())
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);

            assertThat(response.getErrors()).containsExactly(
                "Enter local authority's group email address in the correct format, for example name@example.com",
                "Enter local authority solicitor's email address in the correct format, for example name@example.com");
        }

        @Test
        void shouldAddCourtRelatedFields() {

            final LocalAuthoritiesEventData initialEventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER)
                .localAuthorityToTransfer(LocalAuthority.builder()
                    .email("test1@test.com")
                    .build())
                .localAuthorityToTransferSolicitor(Colleague.builder()
                    .email("test2@test.com")
                    .build())
                .localAuthoritiesToTransfer(dynamicLists.from(1,
                    of(LOCAL_AUTHORITY_2_NAME, LOCAL_AUTHORITY_2_CODE),
                    of(LOCAL_AUTHORITY_3_NAME, LOCAL_AUTHORITY_3_CODE)))
                .build();

            final CaseData initialCaseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .court(COURT_1)
                .localAuthoritiesEventData(initialEventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(initialCaseData, callback);

            assertThat(response.getErrors()).isNull();

            final CaseData updatedCaseData = extractCaseData(response);

            final LocalAuthoritiesEventData expectedEventData = initialEventData.toBuilder()
                .currentCourtName(COURT_1.getName())
                .courtsToTransfer(dynamicLists.from(
                    of(COURT_3A.getName(), COURT_3A.getCode()),
                    of(COURT_3B.getName(), COURT_3B.getCode())))
                .build();

            assertThat(updatedCaseData.getLocalAuthoritiesEventData()).isEqualTo(expectedEventData);
        }
    }

    @Nested
    class TransferToAnotherCourt {

        private final String callback = "transfer-court/court-selection";

        @Test
        void shouldReturnValidateErrorsWhenSelectedCourtRegion() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .currentCourtName(COURT_1.getName())
                .courtsToTransferWithoutTransferLA(dynamicLists.from(0,
                    of(COURT_SEPARATOR.getName(), COURT_SEPARATOR.getCode()),
                    of(COURT_1.getName(), COURT_1.getCode())))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);
            assertThat(response.getErrors()).containsExactly(
                "Invalid court selected.");
        }

        @Test
        void shouldNotReturnValidateErrorsWhenSelectedAnyCourts() {
            final LocalAuthoritiesEventData eventData = LocalAuthoritiesEventData.builder()
                .localAuthorityAction(TRANSFER_COURT)
                .currentCourtName(COURT_1.getName())
                .courtsToTransferWithoutTransferLA(dynamicLists.from(1,
                    of(COURT_SEPARATOR.getName(), COURT_SEPARATOR.getCode()),
                    of(COURT_3A.getName(), COURT_3A.getCode())))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthoritiesEventData(eventData)
                .build();

            final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, callback);
            assertThat(response.getErrors()).isEmpty();
        }

    }
}
