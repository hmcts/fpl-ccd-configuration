package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.RespondentSolicitorNoticeOfChangeTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {NoticeOfChangeContentProvider.class, LookupTestConfig.class})
public class NoticeOfChangeEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    private static final String CASE_NAME = "Test";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Smith";
    public static final CaseData CASE_DATA = CaseData.builder().id(12345L).caseName(CASE_NAME).build();

    @Autowired
    private NoticeOfChangeContentProvider underTest;


    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldReturnExpectedMapForSolicitorAccessGranted(String firstName, String lastName,
                                                          String expectedSalutation) {

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(firstName)
            .lastName(lastName)
            .build();

        RespondentSolicitorNoticeOfChangeTemplate expectedTemplate = buildExpectedTemplate(expectedSalutation)
            .caseUrl("http://fake-url/cases/case-details/" + CASE_ID)
            .build();

        assertThat(underTest.buildRespondentSolicitorAccessGrantedNotification(CASE_DATA, respondentSolicitor))
            .isEqualTo(expectedTemplate);
    }

    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldReturnExpectedMapForSolicitorAccessRevoked(String firstName, String lastName,
                                                          String expectedSalutation) {

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(firstName)
            .lastName(lastName)
            .build();

        RespondentSolicitorNoticeOfChangeTemplate expectedTemplate = buildExpectedTemplate(expectedSalutation).build();

        assertThat(underTest.buildRespondentSolicitorAccessRevokedNotification(CASE_DATA, respondentSolicitor))
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

    private RespondentSolicitorNoticeOfChangeTemplate.RespondentSolicitorNoticeOfChangeTemplateBuilder
        buildExpectedTemplate(String expectedSalutation) {
        return RespondentSolicitorNoticeOfChangeTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString());
    }

}
