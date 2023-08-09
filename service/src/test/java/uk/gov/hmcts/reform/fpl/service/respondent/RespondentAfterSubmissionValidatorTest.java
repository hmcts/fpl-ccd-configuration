package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
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

    @InjectMocks
    private RespondentAfterSubmissionValidator underTest;

    @Nested
    @TestInstance(PER_CLASS)
    class ValidateLegalRepresentation {
        Respondent respondentWithRepresentation = Respondent.builder().legalRepresentation(YES.getValue()).build();

        @Test
        void shouldNotReturnErrorIfNoRespondents() {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of()).build());

            assertThat(actual).isEmpty();
        }

        Stream<Arguments> shouldReturnErrorIfNoLegalRepresentationSelectedParam() {
            return Stream.of(
                Arguments.of(false,
                    "Confirm if respondent 1 has legal representation"),
                Arguments.of(true,
                    "Confirm if respondent has legal representation")
            );
        }

        @ParameterizedTest
        @MethodSource("shouldReturnErrorIfNoLegalRepresentationSelectedParam")
        void shouldReturnErrorIfNoLegalRepresentationSelected(boolean hideRespondentIndex, String message) {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(Respondent.builder().build()))).build(),
                hideRespondentIndex);

            assertThat(actual).containsExactly(message);
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

        Stream<Arguments> shouldReturnErrorsIfDetailsMissingParam() {
            return Stream.of(
                Arguments.of(false, new String[] {
                    "Add the full name of respondent 1's legal representative",
                    "Add the email address of respondent 1's legal representative",
                    "Add the organisation details for respondent 1's representative"
                }),
                Arguments.of(true, new String[] {
                    "Add the full name of respondent's legal representative",
                    "Add the email address of respondent's legal representative",
                    "Add the organisation details for respondent's representative"
                })
            );
        }

        @ParameterizedTest
        @MethodSource("shouldReturnErrorsIfDetailsMissingParam")
        void shouldReturnErrorsIfDetailsMissing(boolean hideRespondentIndex, String[] messages) {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder().build())
                    .build())))
                    .build(), hideRespondentIndex);

            assertThat(actual).containsExactly(messages);
        }

        Stream<Arguments> shouldReturnErrorIfFirstNameMissingParam() {
            return Stream.of(
                Arguments.of(false,
                    "Add the full name of respondent 1's legal representative"),
                Arguments.of(true,
                    "Add the full name of respondent's legal representative")
            );
        }

        @ParameterizedTest
        @MethodSource("shouldReturnErrorIfFirstNameMissingParam")
        void shouldReturnErrorIfFirstNameMissing(boolean hideRespondentIndex, String message) {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .lastName("Smith")
                        .email("john@smith.com")
                        .organisation(Organisation.builder().organisationID("test ID").build())
                        .build())
                    .build())))
                    .build(), hideRespondentIndex);

            assertThat(actual).containsExactly(message);
        }

        Stream<Arguments> shouldReturnErrorIfLastNameMissingParam() {
            return Stream.of(
                Arguments.of(false,
                    "Add the full name of respondent 1's legal representative"),
                Arguments.of(true,
                    "Add the full name of respondent's legal representative")
            );
        }

        @ParameterizedTest
        @MethodSource("shouldReturnErrorIfLastNameMissingParam")
        void shouldReturnErrorIfLastNameMissing(boolean hideRespondentIndex, String message) {
            List<String> actual = underTest.validateLegalRepresentation(
                CaseData.builder().respondents1(List.of(element(respondentWithRepresentation.toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("John")
                        .email("john@smith.com")
                        .organisation(Organisation.builder().organisationID("test ID").build())
                        .build())
                    .build())))
                    .build(), hideRespondentIndex);

            assertThat(actual).containsExactly(message);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void shouldNotReturnErrorWhenNoChanges(boolean hideOnRespondent) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(), hideOnRespondent);

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

    private static Stream<Arguments>
        shouldReturnErrorWhenLegalRepresentationRemovedParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot remove respondent 1's legal representative"),
            Arguments.of(true,
                "You cannot remove respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenLegalRepresentationRemovedParam")
    void shouldReturnErrorWhenLegalRepresentationRemoved(boolean hideRespondentIndex, String message) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, Respondent.builder().legalRepresentation(NO.getValue()).build())))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
    }

    private static Stream<Arguments>
        shouldNotReturnErrorWhenLegalRepresentationRemovedParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot remove respondent 1's legal representative"),
            Arguments.of(true,
                "You cannot remove respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldNotReturnErrorWhenLegalRepresentationRemovedParam")
    void shouldNotReturnErrorWhenLegalRepresentationRemoved(boolean hideRespondentIndex, String message) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, Respondent.builder().legalRepresentation(NO.getValue()).build())))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1).toBuilder()
                    .legalRepresentation(YES.getValue())
                    .build())))
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
    }

    private static Stream<Arguments>
        shouldReturnErrorWhenSolicitorOrganisationModifiedParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot change organisation details for respondent 1's legal representative"),
            Arguments.of(true,
                "You cannot change organisation details for respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenSolicitorOrganisationModifiedParam")
    void shouldReturnErrorWhenSolicitorOrganisationModified(boolean hideRespondentIndex, String message) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_2))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
    }

    private static Stream<Arguments>
        shouldReturnErrorWhenSolicitorOrganisationDeletedParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot change organisation details for respondent 1's legal representative"),
            Arguments.of(true,
                "You cannot change organisation details for respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenSolicitorOrganisationDeletedParam")
    void shouldReturnErrorWhenSolicitorOrganisationDeleted(boolean hideRespondentIndex, String message) {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(null))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
    }

    private static Stream<Arguments>
        shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondentsParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot change organisation details for respondent 2's legal representative"),
            Arguments.of(true,
                "You cannot change organisation details for respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondentsParam")
    void shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondents(boolean hideRespondentIndex,
                                                                                  String message) {
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
                .build(), hideRespondentIndex);

        assertThat(actual)
            .containsExactly(message);
    }

    private static Stream<Arguments>
        shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondentsParam() {
        return Stream.of(
            Arguments.of(false,
                new String[] {
                    "You cannot change organisation details for respondent 1's legal representative",
                    "You cannot change organisation details for respondent 2's legal representative"
                }),
            Arguments.of(true,
                new String[] {
                    "You cannot change organisation details for respondent's legal representative",
                    "You cannot change organisation details for respondent's legal representative"
                })
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondentsParam")
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondents(boolean hideRespondentIndex,
                                                                                           String[] messages) {
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
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(messages);
    }

    private static Stream<Arguments>
        shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondentsParam() {
        return Stream.of(
            Arguments.of(false,
                "You cannot change organisation details for respondent 2's legal representative"),
            Arguments.of(true,
                "You cannot change organisation details for respondent's legal representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondentsParam")
    void shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondents(boolean hideRespondentIndex,
                                                                                  String message) {
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
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
    }

    private static Stream<Arguments>
        shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondentsParam() {
        return Stream.of(
            Arguments.of(false, new String[] {
                "You cannot change organisation details for respondent 1's legal representative",
                "You cannot change organisation details for respondent 2's legal representative"
            }),
            Arguments.of(true, new String[] {
                "You cannot change organisation details for respondent's legal representative",
                "You cannot change organisation details for respondent's legal representative"
            })
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondentsParam")
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondents(
        boolean hideRespondentIndex, String[] messages) {
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
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(messages);
    }

    private static Stream<Arguments> shouldReturnOnlyLegalRepresentationErrorsWhenUserIsAdminParams() {
        return Stream.of(
            Arguments.of(false, "Add the organisation details for respondent 1's representative"),
            Arguments.of(true, "Add the organisation details for respondent's representative")
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnOnlyLegalRepresentationErrorsWhenUserIsAdminParams")
    void shouldReturnOnlyLegalRepresentationErrorsWhenUserIsAdmin(boolean hideRespondentIndex, String message) {
        given(userService.isHmctsAdminUser()).willReturn(true);

        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(null))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, respondentWithRegisteredSolicitor(ORGANISATION_ID_1))))
                .build(), hideRespondentIndex);

        assertThat(actual).containsExactly(message);
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
