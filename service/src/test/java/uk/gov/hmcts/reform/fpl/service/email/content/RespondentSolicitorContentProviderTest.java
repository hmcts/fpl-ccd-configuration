package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {RespondentSolicitorContentProvider.class, LookupTestConfig.class})
class RespondentSolicitorContentProviderTest extends AbstractEmailContentProviderTest {

    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Smith";

    @Autowired
    private RespondentSolicitorContentProvider underTest;

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void shouldReturnExpectedMapWithRepresentativeNameAndLocalAuthorityName(
        String firstName, String lastName, String expectedFullName) {
        RespondentSolicitorTemplate expectedTemplate = buildRegisteredSolicitorTemplate(expectedFullName);

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(firstName)
            .lastName(lastName)
            .organisation(Organisation.builder().organisationID("123").build())
            .build();
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder().solicitor(respondentSolicitor).build());

        CaseData caseData = populatedCaseData(Map.of("respondents1", respondents));

        assertThat(underTest.buildRespondentSolicitorSubmissionNotification(caseData, respondentSolicitor))
            .usingRecursiveComparison().isEqualTo(expectedTemplate);
    }

    private static Stream<Arguments> representativeNameSource() {
        final String salutation = "Dear ";
        final String expectedLastName = salutation + LAST_NAME;
        final String expectedFirstName = salutation + FIRST_NAME;
        final String expectedFullName = salutation + FIRST_NAME + " " + LAST_NAME;

        return Stream.of(
            Arguments.of(null, LAST_NAME, expectedLastName),
            Arguments.of(EMPTY, LAST_NAME, expectedLastName),
            Arguments.of(FIRST_NAME, null, expectedFirstName),
            Arguments.of(FIRST_NAME, EMPTY, expectedFirstName),
            Arguments.of(null, null, ""),
            Arguments.of(EMPTY, EMPTY, ""),
            Arguments.of(FIRST_NAME, LAST_NAME, expectedFullName)
        );
    }

    private RespondentSolicitorTemplate buildRegisteredSolicitorTemplate(String expectedFullName) {
        return RespondentSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .salutation(expectedFullName)
            .build();
    }
}
