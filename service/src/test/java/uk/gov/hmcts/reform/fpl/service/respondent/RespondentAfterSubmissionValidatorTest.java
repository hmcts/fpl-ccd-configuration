package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RespondentAfterSubmissionValidatorTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final Respondent RESPONDENT_1 = Respondent.builder().legalRepresentation(NO.getValue()).build();
    private static final Respondent RESPONDENT_2 = Respondent.builder().legalRepresentation(NO.getValue()).build();
    private static final String ORGANISATION_ID_1 = "OrganisationId1";
    private static final String ORGANISATION_ID_2 = "OrganisationId2";

    @Mock
    private UserService userService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private RespondentAfterSubmissionValidator underTest;

    @Nested
    class ValidateLegalRepresentation {
        Respondent respondentWithRepresentation = Respondent.builder().legalRepresentation(YES.getValue()).build();

        @Test
        void shouldNotReturnErrorIfNoRespondents() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of()).build());

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldReturnErrorIfNoLegalRepresentationSelected() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(Respondent.builder().build()))).build());

            assertThat(actual).containsExactly("Confirm if respondent 1 has legal representation");
        }

        @Test
        void shouldNotReturnErrorIfAllDetailsEntered() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .email("john@smith.com")
                        .organisation(Organisation.builder().organisationID("test ID").build())
                        .build())
                    .build())))
                    .build());

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldNotReturnErrorIfUnregisteredOrganisationPresent() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .email("john@smith.com")
                        .unregisteredOrganisation(UnregisteredOrganisation.builder().name("test org name").build())
                        .build())
                    .build())))
                    .build());

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldReturnErrorsIfDetailsMissing() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder().build())
                    .build())))
                    .build());

            assertThat(actual).containsExactly(
                "Add the full name of respondent 1's legal representative",
                "Add the email address of respondent 1's legal representative",
                "Add the organisation details for respondent 1's representative"
            );
        }

        @Test
        void shouldReturnErrorIfFirstNameMissing() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .lastName("Smith")
                        .email("john@smith.com")
                        .organisation(Organisation.builder().organisationID("test ID").build())
                        .build())
                    .build())))
                    .build());

            assertThat(actual).containsExactly("Add the full name of respondent 1's legal representative");
        }

        @Test
        void shouldReturnErrorIfLastNameMissing() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("John")
                        .email("john@smith.com")
                        .organisation(Organisation.builder().organisationID("test ID").build())
                        .build())
                    .build())))
                    .build());

            assertThat(actual).containsExactly("Add the full name of respondent 1's legal representative");
        }
    }

    @Test
    void shouldNotReturnErrorWhenNoChanges() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotReturnErrorWhenNewRespondentAddedToEmpty(List<Element<Respondent>> respondents) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(respondents)
                .build());

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenNewRespondentAddedToExisting() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenRespondentRemoved() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2)))
                .build());

        assertThat(actual).containsExactly("You cannot remove a respondent from the case");
    }

    @Test
    void shouldNotReturnErrorWhenRepresentationAddedToExistingRespondent() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenRespondentWithRegisteredSolicitorUnchanged() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenLegalRepresentationRemoved() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, Respondent.builder().legalRepresentation(NO.getValue()).build())))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).containsExactly("You cannot remove respondent 1's legal representative");
    }

    @Test
    void shouldNotReturnErrorWhenLegalRepresentationRemoved() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, Respondent.builder().legalRepresentation(NO.getValue()).build())))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1).toBuilder()
                    .legalRepresentation(YES.getValue())
                    .build())))
                .build());

        assertThat(actual).containsExactly("You cannot remove respondent 1's legal representative");
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationModified() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).containsExactly(
            "You cannot change organisation details for respondent 1's legal representative");
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeleted() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(null))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).containsExactly(
            "You cannot change organisation details for respondent 1's legal representative");
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_2))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual)
            .containsExactly("You cannot change organisation details for respondent 2's legal representative");
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_2))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).containsExactly(
            "You cannot change organisation details for respondent 1's legal representative",
            "You cannot change organisation details for respondent 2's legal representative"
        );
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1)),
                    element(UUID_2, respondentWithRegisteredSolicitor(null))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_2))
                ))
                .build());

        assertThat(actual).containsExactly(
            "You cannot change organisation details for respondent 2's legal representative");
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(null)),
                    element(UUID_2, respondentWithRegisteredSolicitor(null))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2)),
                    element(UUID_2, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).containsExactly(
            "You cannot change organisation details for respondent 1's legal representative",
            "You cannot change organisation details for respondent 2's legal representative"
        );
    }

    @Test
    void shouldReturnOnlyLegalRepresentationErrorsWhenToggleOnAndUserIsAdmin() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(true);
        given(userService.isHmctsAdminUser()).willReturn(true);

        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(null))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).containsExactly(
            "Add the organisation details for respondent 1's representative");
    }

    private Respondent respondentWithRegisteredSolicitor(String organisationID) {
        return Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Adam")
                .lastName("Jones")
                .email("adam@jones.com")
                .organisation(Organisation.builder()
                    .organisationID(organisationID)
                    .build())
                .build()).build();
    }
}
