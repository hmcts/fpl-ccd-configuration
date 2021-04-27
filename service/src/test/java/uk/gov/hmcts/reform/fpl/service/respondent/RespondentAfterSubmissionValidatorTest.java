package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class RespondentAfterSubmissionValidatorTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final Respondent RESPONDENT_1 = mock(Respondent.class);
    private static final Respondent RESPONDENT_2 = mock(Respondent.class);
    private static final String ORGANISATION_ID_1 = "OrganisationId1";
    private static final String ORGANISATION_ID_2 = "OrganisationId2";

    private final RespondentAfterSubmissionValidator underTest = new RespondentAfterSubmissionValidator();

    @Test
    void shouldNotReturnErrorWhenNoChanges() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEqualTo(List.of());
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

        assertThat(actual).isEqualTo(List.of());
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

        assertThat(actual).isEqualTo(List.of());
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

        assertThat(actual).isEqualTo(List.of("Removing an existing respondent is not allowed"));
    }

    @Test
    void shouldNotReturnErrorWhenRepresentationAddedToExistingRespondent() {
        Respondent updatedRespondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .organisation(Organisation.builder()
                    .organisationID(ORGANISATION_ID_1)
                    .build())
                .build()).build();

        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, updatedRespondent)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldNotReturnErrorWhenSolicitorOrganisationAdded() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1))))
                .build(),
            CaseData.builder()
                .respondents1(List.of())
                .build());

        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldReturnErrorWhenLegalRepresentationRemoved() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, Respondent.builder().legalRepresentation(NO.getValue()).build())))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1).toBuilder()
                    .legalRepresentation(YES.getValue())
                    .build())))
                .build());

        assertThat(actual).isEqualTo(List.of("You cannot remove respondent 1's legal representative"));
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationModified() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 1 is not allowed"));
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeleted() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(null))))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1))))
                .build());

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 1 is not allowed"));
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_2))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 2 is not allowed"));
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_2))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).isEqualTo(List.of(
            "Change of organisation for respondent 1 is not allowed",
            "Change of organisation for respondent 2 is not allowed"
        ));
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(null))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 2 is not allowed"));
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondents() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(null)),
                    element(UUID_2, solicitorWithOrganisation(null))
                ))
                .build(),
            CaseData.builder()
                .respondents1(List.of(
                    element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                    element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
                ))
                .build());

        assertThat(actual).isEqualTo(List.of(
            "Change of organisation for respondent 1 is not allowed",
            "Change of organisation for respondent 2 is not allowed"
        ));
    }

    private Respondent solicitorWithOrganisation(String organisationID) {
        return Respondent.builder().solicitor(RespondentSolicitor.builder()
            .organisation(Organisation.builder()
                .organisationID(organisationID)
                .build())
            .build()).build();
    }
}
