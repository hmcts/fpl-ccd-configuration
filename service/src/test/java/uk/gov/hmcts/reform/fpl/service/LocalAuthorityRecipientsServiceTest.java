package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsServiceTest.FALLBACK_INBOX;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityEmailLookupConfiguration.class, LocalAuthorityRecipientsService.class})
@TestPropertySource(properties = {"fpl.local_authority_fallback_inbox=" + FALLBACK_INBOX})
class LocalAuthorityRecipientsServiceTest {

    static final String FALLBACK_INBOX = "FamilyPublicLaw@gmail.com";
    private static final String LA_1_INBOX = "la1@gmail.com";
    private static final String LA_2_INBOX = "la2@gmail.com";
    private static final String LA_1_GROUP_EMAIL = "lagroup1@gmail.com";
    private static final String LA_2_GROUP_EMAIL = "lagroup2@gmail.com";
    private static final String LA_1_CODE = "LA1";
    private static final String LA_2_CODE = "LA2";
    private static final String LA_1_ID = "ORG1";
    private static final String LA_2_ID = "ORG22";
    private static final Long CASE_ID = 12345L;

    private final Solicitor legacySolicitor = Solicitor.builder()
        .email("solicitor@test.com")
        .build();

    private final Colleague designatedLAColleague1 = Colleague.builder()
        .email("colleague.1@designated.com")
        .notificationRecipient("No")
        .build();

    private final Colleague designatedLAColleague2 = Colleague.builder()
        .email("colleague.2@designated.com")
        .notificationRecipient("Yes")
        .build();

    private final Colleague designatedLAColleague3 = Colleague.builder()
        .email("colleague.3@designated.com")
        .notificationRecipient("Yes")
        .build();

    private final Colleague secondaryLAColleague1 = Colleague.builder()
        .email("colleague.1@secondary.com")
        .notificationRecipient("No")
        .build();

    private final Colleague secondaryLAColleague2 = Colleague.builder()
        .email("colleague.2@secondary.com")
        .notificationRecipient("Yes")
        .build();

    private final Colleague secondaryLAColleague3 = Colleague.builder()
        .email("colleague.3@secondary.com")
        .notificationRecipient("Yes")
        .build();

    private final LegalRepresentative legalRepresentative1 = LegalRepresentative.builder()
        .email("representative.1@solicitors.com")
        .build();

    private final LegalRepresentative legalRepresentative2 = LegalRepresentative.builder()
        .email("representative.2@solicitors.com")
        .build();

    @MockBean
    private FeatureToggleService featureToggles;

    @MockBean
    private LocalAuthorityIdLookupConfiguration localAuthorityIds;

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmails;

    @Autowired
    private LocalAuthorityRecipientsService underTest;

    @BeforeEach
    void init() {
        given(localAuthorityIds.getLocalAuthorityCode(LA_1_ID)).willReturn(Optional.of(LA_1_CODE));
        given(localAuthorityIds.getLocalAuthorityCode(LA_2_ID)).willReturn(Optional.of(LA_2_CODE));
    }

    @Test
    void shouldReturnFallbackWhenNoEmailsFoundForTheLocalAuthority() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LA_1_CODE)
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .id(LA_1_ID)
                .designated(YES.getValue())
                .email(EMPTY)
                .build()))
            .build();

        given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

        assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
    }

    @Nested
    class DesignatedLocalAuthorityContacts {

        @Test
        void shouldNotSendToOnboardingConfigIfRestrictedCase() {
            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .email(LA_2_INBOX)
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build()))
                .build();

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            assertThat(underTest.getRecipients(recipientsRequest))
                .containsExactlyInAnyOrder(LA_2_INBOX, designatedLAColleague2.getEmail(),
                    designatedLAColleague3.getEmail());
        }

        @Test
        void shouldNotReturnLocalAuthorityEmailsWhenDesignatedLocalAuthorityExcluded() {
            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .email(LA_1_INBOX)
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build()))
                .build();

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .build();

            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
        }

        @Test
        void shouldReturnFirstLocalAuthorityWhenDesignatedIsNotSet() {
            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .email(LA_1_INBOX)
                        .designated("No")
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .email(LA_2_INBOX)
                        .designated("No")
                        .colleagues(wrapElements(secondaryLAColleague1, secondaryLAColleague2, secondaryLAColleague3))
                        .build()))
                .build();

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(false)
                .build();

            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(LA_1_INBOX);
        }

        @Test
        void shouldNotReturnLocalAuthorityEmailsWhenNoGroupEmailOrSharedInboxOrColleagues() {
            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .colleagues(wrapElements())
                    .build()))
                .build();

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
        }

        @Test
        void shouldReturnLocalAuthorityGroupEmailWhenAdditionalContactsAreToggledOff() {
            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .email(LA_1_GROUP_EMAIL)
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(false);
            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                LA_1_GROUP_EMAIL
            );
        }

        @Test
        void shouldReturnGroupEmailOnlyWhenNoNotificationRecipientsAmongColleagues() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .email(LA_1_GROUP_EMAIL)
                    .colleagues(wrapElements(designatedLAColleague1))
                    .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                LA_1_GROUP_EMAIL
            );
        }

        @Test
        void shouldReturnAdditionalContactEmailsOnly() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                designatedLAColleague2.getEmail(),
                designatedLAColleague3.getEmail());
        }

        @Test
        void shouldReturnGroupEmailAndAdditionalContactsEmails() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated(YES.getValue())
                    .email(LA_1_GROUP_EMAIL)
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                LA_1_GROUP_EMAIL,
                designatedLAColleague2.getEmail(),
                designatedLAColleague3.getEmail());
        }

        @Test
        void shouldReturnContactFromLegacySolicitor() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .build();

            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);
            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                legacySolicitor.getEmail());
        }

    }

    @Nested
    class SecondaryLocalAuthorityContacts {

        @BeforeEach
        void init() {
            given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);
        }

        @Test
        void shouldNotReturnContactsWhenSecondaryLocalAuthorityExcluded() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .designated(YES.getValue())
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .designated(NO.getValue())
                        .email(LA_2_INBOX)
                        .colleagues(wrapElements(secondaryLAColleague1, secondaryLAColleague2, secondaryLAColleague3))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.of(LA_2_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(true)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
        }

        @Test
        void shouldNotReturnSecondaryLAContactsWhenAdditionalContactsToggledOffAndGroupAndSharedLAEmailNotPresent() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .email(LA_1_INBOX)
                        .designated("Yes")
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .email(EMPTY)
                        .designated("No")
                        .colleagues(wrapElements(secondaryLAColleague1, secondaryLAColleague2, secondaryLAColleague3))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.empty());
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(false);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(false)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(LA_1_INBOX);
        }

        @Test
        void shouldNotReturnContactsWhenNoSecondaryLocalAuthority() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .designated("Yes")
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(false)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
        }

        @Test
        void shouldNotReturnAdditionalEmailWhenNoNotificationRecipientsAmongColleagues() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .designated("Yes")
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .designated("No")
                        .email(LA_2_GROUP_EMAIL)
                        .colleagues(wrapElements(secondaryLAColleague1))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
            given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.of(LA_2_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(false)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                LA_2_INBOX,
                LA_2_GROUP_EMAIL
            );
        }

        @Test
        void shouldReturnSecondaryLocalAuthorityGroupInboxAndSharedInbox() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .designated("Yes")
                        .email(LA_1_INBOX)
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .designated("No")
                        .email(LA_2_GROUP_EMAIL)
                        .colleagues(wrapElements(secondaryLAColleague1))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.of(LA_2_INBOX));
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(false);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(false)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                LA_1_INBOX,
                LA_2_GROUP_EMAIL,
                LA_2_INBOX
            );
        }

        @Test
        void shouldReturnAdditionalEmailFromSecondaryLocalAuthority() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(
                    LocalAuthority.builder()
                        .id(LA_1_ID)
                        .designated("Yes")
                        .colleagues(wrapElements(designatedLAColleague1))
                        .build(),
                    LocalAuthority.builder()
                        .id(LA_2_ID)
                        .designated("No")
                        .colleagues(wrapElements(secondaryLAColleague1, secondaryLAColleague2, secondaryLAColleague3))
                        .build()))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());
            given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.empty());
            given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(true);

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .secondaryLocalAuthorityExcluded(false)
                .caseData(caseData).build();

            assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
                secondaryLAColleague2.getEmail(),
                secondaryLAColleague3.getEmail());
        }
    }

    @Nested
    class LegalRepresentatives {

        @Test
        void shouldNotReturnContactsWhenLegalRepresentativesExcluded() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .legalRepresentatives(wrapElements(legalRepresentative1, legalRepresentative2))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .legalRepresentativesExcluded(true)
                .caseData(caseData).build();


            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(FALLBACK_INBOX);
        }

        @Test
        void shouldReturnContactsWhenLegalRepresentativesIsNotExcluded() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(LA_1_CODE)
                .legalRepresentatives(wrapElements(legalRepresentative1, legalRepresentative2))
                .build();

            given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.empty());

            final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
                .legalRepresentativesExcluded(false)
                .caseData(caseData).build();


            assertThat(underTest.getRecipients(recipientsRequest)).containsExactly(
                legalRepresentative1.getEmail(),
                legalRepresentative2.getEmail());
        }

    }

    @Test
    void shouldReturnCombinedListOfRecipients() {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LA_1_CODE)
            .localAuthorities(wrapElements(
                LocalAuthority.builder()
                    .id(LA_1_ID)
                    .designated("Yes")
                    .email(LA_1_GROUP_EMAIL)
                    .colleagues(wrapElements(designatedLAColleague1, designatedLAColleague2, designatedLAColleague3))
                    .build(),
                LocalAuthority.builder()
                    .id(LA_2_ID)
                    .designated("No")
                    .email(LA_2_INBOX)
                    .colleagues(wrapElements(secondaryLAColleague1, secondaryLAColleague2, secondaryLAColleague3))
                    .build()))
            .legalRepresentatives(wrapElements(legalRepresentative1, legalRepresentative2))
            .build();

        given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(Optional.of(LA_1_INBOX));
        given(localAuthorityEmails.getSharedInbox(LA_2_CODE)).willReturn(Optional.of(LA_2_INBOX));
        given(featureToggles.emailsToSolicitorEnabled(LA_1_CODE)).willReturn(true);
        given(featureToggles.emailsToSolicitorEnabled(LA_2_CODE)).willReturn(true);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData).build();

        assertThat(underTest.getRecipients(recipientsRequest)).containsExactlyInAnyOrder(
            LA_1_GROUP_EMAIL,
            LA_2_INBOX,
            designatedLAColleague2.getEmail(),
            designatedLAColleague3.getEmail(),
            secondaryLAColleague2.getEmail(),
            secondaryLAColleague3.getEmail(),
            legalRepresentative1.getEmail(),
            legalRepresentative2.getEmail()
        );
    }

    @Test
    void shouldReturnShareInboxOfGivenLA() {
        Optional<String> expected = Optional.of(LA_1_GROUP_EMAIL);
        given(localAuthorityEmails.getSharedInbox(LA_1_CODE)).willReturn(expected);
        assertThat(underTest.getShareInbox(LocalAuthority.builder().id(LA_1_ID).build())).isEqualTo(expected);
    }
}
