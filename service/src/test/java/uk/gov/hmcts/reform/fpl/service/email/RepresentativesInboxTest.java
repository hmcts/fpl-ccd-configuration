package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RepresentativesInboxTest {

    private static final String EMAIL_1 = "email1";
    private static final String EMAIL_2 = "email2";
    private static final String EMAIL_3 = "email3";
    private static final String EMAIL_4 = "email4";
    private static final String ORGANISATION_ID = "ORGANISATION_ID";

    private final RepresentativesInbox underTest = new RepresentativesInbox();

    @Test
    void testRepresentativesByPOSTIsNotAccepted() {
        CaseData caseData = CaseData.builder().build();

        assertThrows(IllegalArgumentException.class,
            () -> underTest.getEmailsByPreference(caseData, POST));
    }

    @Test
    void testNullRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(null).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testNoRepresentatives() {
        CaseData caseData = CaseData.builder().representatives(wrapElements()).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentatives() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email(EMAIL_1)
                    .servingPreferences(EMAIL)
                    .build(),
                Representative.builder()
                    .email(EMAIL_2)
                    .servingPreferences(DIGITAL_SERVICE)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRegisteredRespondentSolicitors() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(Organisation.builder()
                        .organisationID(ORGANISATION_ID)
                        .build())
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(Organisation.builder()
                        .build())
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromRegisteredRespondentSolicitors() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(Organisation.builder()
                        .organisationID(ORGANISATION_ID)
                        .build())
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyDigitalRepresentativesFromRespondentSolicitorsUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(Organisation.builder()
                        .build())
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenUnregistered() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(Organisation.builder()
                        .organisationID(null)
                        .build())
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenNoOrganisation() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .email(EMAIL_1)
                    .organisation(null)
                    .build())
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyEmailRepresentativesFromRespondentSolicitorsWhenNoSolicitor() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(
            Respondent.builder()
                .solicitor(null)
                .build()
        )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveDuplicates() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email(EMAIL_1)
                    .servingPreferences(EMAIL)
                    .build(),
                Representative.builder()
                    .email(EMAIL_1)
                    .servingPreferences(EMAIL)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1
        ));
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveNulls() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email(null)
                    .servingPreferences(EMAIL)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyEmailRepresentativesRemoveEmpty() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email("")
                    .servingPreferences(EMAIL)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData,
            EMAIL);

        assertThat(actual).isEqualTo(Set.of());
    }

    @Test
    void testFilterOnlyDigitalRepresentatives() {
        CaseData caseData = CaseData.builder()
            .representatives(wrapElements(
                Representative.builder()
                    .email(EMAIL_1)
                    .servingPreferences(EMAIL)
                    .build(),
                Representative.builder()
                    .email(EMAIL_2)
                    .servingPreferences(DIGITAL_SERVICE)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData,
            DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_2
        ));
    }

    @Test
    void testFilterOnlyDigitalMixedRepresentatives() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_1)
                        .organisation(null)
                        .build())
                    .build(),
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_2)
                        .organisation(Organisation.builder()
                            .organisationID(ORGANISATION_ID)
                            .build())
                        .build())
                    .build()
            ))
            .representatives(wrapElements(
                Representative.builder()
                    .email(EMAIL_3)
                    .servingPreferences(EMAIL)
                    .build(),
                Representative.builder()
                    .email(EMAIL_4)
                    .servingPreferences(DIGITAL_SERVICE)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData,
            DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_2, EMAIL_4
        ));
    }

    @Test
    void testFilterOnlyEmailMixedRepresentatives() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_1)
                        .organisation(null)
                        .build())
                    .build(),
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_2)
                        .organisation(Organisation.builder()
                            .organisationID(ORGANISATION_ID)
                            .build())
                        .build())
                    .build()
            ))
            .representatives(wrapElements(
                Representative.builder()
                    .email(EMAIL_3)
                    .servingPreferences(EMAIL)
                    .build(),
                Representative.builder()
                    .email(EMAIL_4)
                    .servingPreferences(DIGITAL_SERVICE)
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreference(caseData,
            EMAIL);

        assertThat(actual).isEqualTo(Set.of(
            EMAIL_1, EMAIL_3
        ));
    }

    @Test
    void shouldGetEmailsByPreferenceEmailExcludingOthers() {
        final Element<Representative> representativeServedByPost = element(Representative.builder()
            .fullName("Representative 1")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(POST)
            .build());

        final Element<Representative> representativeServedByEmail = element(Representative.builder()
            .fullName("Representative 2")
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.BARRISTER)
            .email(EMAIL_2)
            .build());

        final Element<Representative> representativeServedByEmail2 = element(Representative.builder()
            .fullName("Representative 3")
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .email(EMAIL_3)
            .build());

        final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
            .fullName("Representative 4")
            .servingPreferences(DIGITAL_SERVICE)
            .role(RepresentativeRole.BARRISTER)
            .build());

        final CaseData caseData = CaseData.builder()
            .representatives(List.of(
                representativeServedByPost,
                representativeServedByEmail,
                representativeServedByEmail2,
                representativeServedByDigitalService))
            .build();

        Set<String> actual = underTest.getEmailsByPreferenceExcludingOthers(caseData, EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_2));
    }

    @Test
    void shouldGetEmailsByPreferenceDigitalExcludingOthers() {
        final Element<Representative> representativeServedByPost = element(Representative.builder()
            .fullName("Representative 1")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(POST)
            .build());

        final Element<Representative> representativeServedByEmail = element(Representative.builder()
            .fullName("Representative 2")
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.BARRISTER)
            .build());

        final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
            .fullName("Representative 3")
            .servingPreferences(DIGITAL_SERVICE)
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .build());

        final Element<Representative> representativeServedByDigitalService2 = element(Representative.builder()
            .fullName("Representative 4")
            .servingPreferences(DIGITAL_SERVICE)
            .role(RepresentativeRole.BARRISTER)
            .email(EMAIL_4)
            .build());

        final CaseData caseData = CaseData.builder()
            .representatives(List.of(
                representativeServedByPost,
                representativeServedByEmail,
                representativeServedByDigitalService,
                representativeServedByDigitalService2))
            .build();

        Set<String> actual = underTest.getEmailsByPreferenceExcludingOthers(caseData, DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_4));
    }

    @Test
    void shouldNotAcceptPOSTAsPreference() {
        CaseData caseData = CaseData.builder().build();

        assertThrows(IllegalArgumentException.class,
            () -> underTest.getEmailsByPreferenceExcludingOthers(caseData, POST));
    }

    @Test
    void shouldGetRespondentSolicitorEmails() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_1)
                        .organisation(null)
                        .build())
                    .build(),
                Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .email(EMAIL_2)
                        .organisation(Organisation.builder()
                            .organisationID(ORGANISATION_ID)
                            .build())
                        .build())
                    .build()
            )).build();

        Set<String> actual = underTest.getEmailsByPreferenceExcludingOthers(caseData,
            DIGITAL_SERVICE);

        assertThat(actual).isEqualTo(Set.of(EMAIL_2));
    }

    @Test
    void shouldGetOtherRepresentativesToBeNotifiedByEmail() {
        UUID representativeID = UUID.randomUUID();
        final Element<Representative> representativeServedByEmail1 = element(representativeID, Representative.builder()
            .fullName("Representative 1")
            .email(EMAIL_1)
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(EMAIL)
            .build());

        final Element<Representative> representativeServedByEmail2 = element(Representative.builder()
            .fullName("Representative 2")
            .servingPreferences(EMAIL)
            .role(RepresentativeRole.BARRISTER)
            .build());

        final Element<Representative> representativeServedByEmail3 = element(Representative.builder()
            .fullName("Representative 3")
            .role(RepresentativeRole.REPRESENTING_OTHER_PERSON_1)
            .servingPreferences(EMAIL)
            .build());

        final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
            .fullName("Representative 4")
            .servingPreferences(DIGITAL_SERVICE)
            .build());

        final CaseData caseData = CaseData.builder()
            .representatives(List.of(
                representativeServedByEmail1,
                representativeServedByEmail2,
                representativeServedByEmail3,
                representativeServedByDigitalService))
            .build();

        Other firstOther = Other.builder()
            .name("Other 1")
            .build();

        firstOther.addRepresentative(representativeID);

        List<Element<Other>> othersSelected = List.of(element(firstOther));

        Set<String> actual = underTest.getOtherRepresentativesToBeNotified(othersSelected,
            caseData.getRepresentatives(),
            EMAIL);

        assertThat(actual).isEqualTo(Set.of(EMAIL_1));
    }

    @Test
    void shouldReturnEmptyListIfNoOtherRepresentativesByDigitalService() {
        UUID representativeID = UUID.randomUUID();
        final Element<Representative> representativeServedByEmail = element(representativeID, Representative.builder()
            .fullName("Representative 1")
            .email(EMAIL_1)
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(EMAIL)
            .build());

        final CaseData caseData = CaseData.builder()
            .representatives(List.of(
                representativeServedByEmail))
            .build();

        List<Element<Other>> othersSelected = emptyList();

        Set<String> actual = underTest.getOtherRepresentativesToBeNotified(othersSelected,
            caseData.getRepresentatives(),
            DIGITAL_SERVICE);

        assertThat(actual).isEmpty();
    }
}
