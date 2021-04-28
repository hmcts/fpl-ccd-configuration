package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {NoticeOfChangeContentProvider.class, LookupTestConfig.class})
class NoticeOfChangeEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    private static final String CASE_NAME = "Test";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final CaseData CASE_DATA = CaseData.builder().id(CASE_ID).caseName(CASE_NAME).build();

    @Autowired
    private NoticeOfChangeContentProvider underTest;


    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldBuildNotificationDataForSolicitorAccessGranted(String firstName, String lastName,
                                                          String expectedSalutation) {

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(firstName)
            .lastName(lastName)
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate = buildExpectedTemplate(expectedSalutation);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondentSolicitor))
            .isEqualTo(expectedTemplate);
    }

    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldBuildNotificationDataForSolicitorAccessRevoked(String firstName, String lastName,
                                                          String expectedSalutation) {

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(firstName)
            .lastName(lastName)
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate = buildExpectedTemplate(expectedSalutation);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondentSolicitor))
            .isEqualTo(expectedTemplate);
    }

    private static Stream<Arguments> solicitorNameSource() {
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

    private NoticeOfChangeRespondentSolicitorTemplate
        buildExpectedTemplate(String expectedSalutation) {
        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseUrl("http://fake-url/cases/case-details/" + CASE_ID)
            .build();
    }
}
