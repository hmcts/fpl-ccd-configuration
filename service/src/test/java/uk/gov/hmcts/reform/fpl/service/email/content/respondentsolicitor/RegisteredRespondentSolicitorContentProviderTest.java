package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.RegisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {RegisteredRespondentSolicitorContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class})
class RegisteredRespondentSolicitorContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String REPRESENTATIVE_FIRST_NAME = "John";
    private static final String REPRESENTATIVE_LAST_NAME = "Smith";
    private static final String RESPONDENT_FIRST_NAME = "Emma";
    private static final String RESPONDENT_LAST_NAME = "Williams";
    private static final String MANAGE_ORG_LINK = "https://manage-org.platform.hmcts.net";
    private static final Long CASE_REFERENCE = 12345L;

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private RegisteredRespondentSolicitorContentProvider underTest;

    private List<Element<Child>> children = wrapElements(mock(Child.class));

    private CaseData caseData = CaseData.builder()
        .id(CASE_REFERENCE)
        .caseName("Test case1")
        .children1(children)
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .build();

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void shouldReturnExpectedMapWithRepresentativeNameAndLocalAuthorityName(
        String firstName, String lastName, String expectedFullName) {
        RegisteredRespondentSolicitorTemplate expectedTemplate = buildRegisteredSolicitorTemplate(
            expectedFullName, String.format("%s %s", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME));

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .solicitor(RespondentSolicitor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .organisation(Organisation.builder().organisationID("123").build())
                .build()).build();

        CaseData caseData = this.caseData.toBuilder().respondents1(wrapElements(respondent)).build();
        when(helper.getEldestChildLastName(children)).thenReturn("Tim Jones");

        assertThat(underTest.buildRespondentSolicitorSubmissionNotification(caseData, respondent))
            .usingRecursiveComparison().isEqualTo(expectedTemplate);
    }

    @Test
    void shouldReturnExpectedMapWithEmptyRespondentName() {
        RegisteredRespondentSolicitorTemplate expectedTemplate
            = buildRegisteredSolicitorTemplate("Dear John Smith", "");

        Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("John")
                .lastName("Smith")
                .organisation(Organisation.builder().organisationID("123").build())
                .build()).build();

        CaseData caseData = this.caseData.toBuilder().respondents1(wrapElements(respondent)).build();
        when(helper.getEldestChildLastName(children)).thenReturn("Tim Jones");

        assertThat(underTest.buildRespondentSolicitorSubmissionNotification(caseData, respondent))
            .usingRecursiveComparison().isEqualTo(expectedTemplate);
    }

    private static Stream<Arguments> representativeNameSource() {
        final String salutation = "Dear ";
        final String expectedLastName = salutation + REPRESENTATIVE_LAST_NAME;
        final String expectedFirstName = salutation + REPRESENTATIVE_FIRST_NAME;
        final String expectedFullName = salutation + REPRESENTATIVE_FIRST_NAME + " " + REPRESENTATIVE_LAST_NAME;

        return Stream.of(
            Arguments.of(null, REPRESENTATIVE_LAST_NAME, expectedLastName),
            Arguments.of(EMPTY, REPRESENTATIVE_LAST_NAME, expectedLastName),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, null, expectedFirstName),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, EMPTY, expectedFirstName),
            Arguments.of(null, null, EMPTY),
            Arguments.of(EMPTY, EMPTY, EMPTY),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, REPRESENTATIVE_LAST_NAME, expectedFullName)
        );
    }

    private RegisteredRespondentSolicitorTemplate buildRegisteredSolicitorTemplate(String expectedSalutation,
                                                                                   String expectedClientName) {
        return RegisteredRespondentSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_1_NAME)
            .salutation(expectedSalutation)
            .clientFullName(expectedClientName)
            .manageOrgLink(MANAGE_ORG_LINK)
            .ccdNumber(CASE_REFERENCE.toString())
            .caseName("Test case1")
            .childLastName("Tim Jones")
            .build();
    }
}
