package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class RespondentAfterSubmissionValidatorTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final Respondent RESPONDENT_1 = mock(Respondent.class);
    private static final Respondent RESPONDENT_2 = mock(Respondent.class);
    private static final String ORGANISATION_ID_1 = "OrganisationId1";
    private static final String ORGANISATION_ID_2 = "OrganisationId2";

    @Mock
    private UserService userService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private RespondentAfterSubmissionValidator underTest;

    @ParameterizedTest
    @MethodSource("toggleAndIsAdmin")
    void shouldNotReturnErrorWhenNoChangesToRespondents(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData(element(UUID_1, RESPONDENT_1))
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("toggleAndIsAdmin")
    void shouldNotReturnErrorWhenNewRespondentAddedToEmpty(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData()
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("toggleAndIsAdmin")
    void shouldNotReturnErrorWhenNewRespondentAddedToExisting(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2)),
            caseData(element(UUID_1, RESPONDENT_1))
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("toggleAndIsAdmin")
    void shouldReturnErrorWhenRespondentRemoved(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, RESPONDENT_1)),
            caseData(element(UUID_1, RESPONDENT_1), element(UUID_2, RESPONDENT_2))
        );

        assertThat(actual).containsExactly("Removing an existing respondent is not allowed");
    }

    @ParameterizedTest
    @MethodSource("toggleAndIsAdmin")
    void shouldNotReturnErrorWhenRepresentationAddedToExistingRespondent(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

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
    @MethodSource("toggleAndIsAdmin")
    void shouldNotReturnErrorWhenSolicitorOrganisationAdded(boolean nocToggle, boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(nocToggle);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1))),
            caseData()
        );

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenSolicitorOrganisationModifiedAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_2))),
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)))
        );

        assertThat(actual).containsExactly("Change of organisation for respondent 1 is not allowed");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenSolicitorOrganisationDeletedAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

        List<String> actual = underTest.validate(
            caseData(element(UUID_1, solicitorWithOrganisation(null))),
            caseData(element(UUID_1, solicitorWithOrganisation(ORGANISATION_ID_1)))
        );

        assertThat(actual).isEqualTo(List.of("Change of organisation for respondent 1 is not allowed"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenSolicitorOrganisationChangedWithMultipleRespondentsAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorsWhenMultipleSolicitorsOrgsChangedWithMultipleRespondentsAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenSolicitorOrgsDeletedWithMultipleRespondentsAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorsWhenMultipleSolicitorOrgsDeletedWithMultipleRespondentsAndNoCDisabled(boolean isAdmin) {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(false);
        given(userService.isHmctsAdminUser()).willReturn(isAdmin);

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
    void shouldNotReturnErrorWhenSolicitorOrgChangedByAdminAndNoticeOfChangeEnabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(true);
        given(userService.isHmctsAdminUser()).willReturn(true);

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

    @Test
    void shouldReturnErrorsWhenSolicitorOrgChangedByNonAdminAndNoticeOfChangeEnabled() {
        given(featureToggleService.isNoticeOfChangeEnabled()).willReturn(true);
        given(userService.isHmctsAdminUser()).willReturn(false);

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

        assertThat(actual).containsExactly(
            "Change of organisation for respondent 1 is not allowed",
            "Change of organisation for respondent 2 is not allowed");
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

    private static Stream<Arguments> toggleAndIsAdmin() {
        return Stream.of(
            Arguments.of(false, false),
            Arguments.of(false, true),
            Arguments.of(true, false),
            Arguments.of(true, true)
        );
    }
}
