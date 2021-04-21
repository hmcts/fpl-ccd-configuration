package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RepresentativesInboxTest {

    private static final String EMAIL_1 = "email1";
    private static final String EMAIL_2 = "email2";
    private static final String EMAIL_3 = "email3";
    private static final String EMAIL_4 = "email4";
    private static final String ORGANISATION_ID = "ORGANISATION_ID";

    private final FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    private final RepresentativesInbox underTest = new RepresentativesInbox(featureToggleService);

    @BeforeEach
    void setUp() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(true);
    }

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
    void testFilterIgnoreRespondentsWhenToggledOff() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(false);

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
            EMAIL_3
        ));
    }


}
