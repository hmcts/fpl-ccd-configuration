package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RespondentAfterSubmissionValidatorTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final Respondent RESPONDENT_1 = mock(Respondent.class);
    private static final Respondent RESPONDENT_2 = mock(Respondent.class);
    private static final String ORGANISATION_ID_1 = "OrganisationId1";
    private static final String ORGANISATION_ID_2 = "OrganisationId2";

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private RespondentAfterSubmissionValidator underTest;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorWhenNoChangesToRespondents(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData(element(UUID_1, RESPONDENT_1))
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorWhenNewRespondentAddedToEmpty(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData()
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorWhenNewRespondentAddedToExisting(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2)),
            caseData(element(UUID_1, RESPONDENT_1))
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenRespondentRemoved(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2))
        );

        assertThat(actual).containsExactly("Removing an existing respondent is not allowed");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorWhenRepresentationAddedToExistingRespondent(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        Respondent updatedRespondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .organisation(Organisation.builder()
                    .organisationID(ORGANISATION_ID_1)
                    .build())
                .build()).build();

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, updatedRespondent)),
            caseData(element(UUID_1, RESPONDENT_1))
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorWhenSolicitorOrganisationAdded(boolean nocToggle) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1))),
            caseData()
        );

        assertThat(actual).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationModifiedAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2))),
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)))
        );

        assertThat(actual).containsExactly("Change of organisation for respondent 1 is not allowed");
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeletedAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(null))),
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)))
        );

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 1 is not allowed"));
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondentsAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_2))
            ),
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
            )
        );

        assertThat(actual).containsExactly("Change of organisation for respondent 2 is not allowed");
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationChangedWithMultipleRespondentsAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_2))
            ),
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
            )
        );

        assertThat(actual).containsExactly(
            "Change of organisation for respondent 1 is not allowed",
            "Change of organisation for respondent 2 is not allowed");
    }

    @Test
    void shouldReturnErrorWhenSolicitorOrganisationDeletedWithMultipleRespondentsAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(null))
            ),
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
            )
        );

        assertThat(actual).containsExactly("Change of organisation for respondent 2 is not allowed");
    }

    @Test
    void shouldReturnErrorsWhenMultipleSolicitorOrganisationDeletedWithMultipleRespondentsAndNoticeOfChangeDisabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);

        List<String> actual = underTest.validate(
            caseData(
                element(UUID_1, solicitorWithOrganisation(null)),
                element(UUID_2, solicitorWithOrganisation(null))
            ),
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
            )
        );

        assertThat(actual).containsExactly(
            "Change of organisation for respondent 1 is not allowed",
            "Change of organisation for respondent 2 is not allowed");
    }

    @Test
    void shouldNotReturnErrorWhenSolicitorOrganisationChangedAndNoticeOfChangeEnabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(true);

        List<String> actual = underTest.validate(
            caseData(
                element(UUID_1, solicitorWithOrganisation(null)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_2))
            ),
            caseData(
                element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2)),
                element(UUID_2, solicitorWithOrganisation(ORGANISATION_ID_1))
            )
        );

        assertThat(actual).isEmpty();
    }

    private static Respondent solicitorWithOrganisation(String organisationID) {
        return Respondent.builder().solicitor(RespondentSolicitor.builder()
            .organisation(Organisation.builder()
                .organisationID(organisationID)
                .build())
            .build()).build();
    }

    @SafeVarargs
    private static CaseData caseData(Element<Respondent>... respondents) {
        return CaseData.builder()
            .respondents1(List.of(respondents))
            .build();
    }
}
