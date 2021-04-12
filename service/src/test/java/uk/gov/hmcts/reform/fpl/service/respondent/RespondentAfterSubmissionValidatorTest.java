package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
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
    void testIfRespondentDidNotChange() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build());

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testIfRespondentIfNewAddedIfNotExisting() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(List.of())
                .build());

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testIfRespondentIfNewAddedIfNotExistingNull() {
        List<String> actual = underTest.validate(
            CaseData.builder()
                .respondents1(List.of(element(UUID_1, RESPONDENT_1)))
                .build(),
            CaseData.builder()
                .respondents1(null)
                .build());

        assertThat(actual).isEqualTo(List.of());

    }

    @Test
    void testIfRespondentIfNewAddedIfExisting() {
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
    void testIfRespondentIfRemoved() {
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
    void testIfRespondentChangedSolicitorOrganisation() {
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
    void testIfRespondentDeletedSolicitorOrganisation() {
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
    void testIfRespondentChangedSolicitorOrganisationWithMultipleRespondents() {
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
    void testIfRespondentChangedMultipleSolicitorOrganisationWithMultipleRespondents() {
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
    void testIfRespondentDeletedSolicitorOrganisationWithMultipleRespondents() {
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
    void testIfRespondentDeletedMultipleSolicitorOrganisationWithMultipleRespondents() {
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
