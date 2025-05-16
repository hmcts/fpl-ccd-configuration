package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.noticeofchange.NoticeOfChangeRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {NoticeOfChangeContentProvider.class, EmailNotificationHelper.class})
class NoticeOfChangeEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;
    private static final String CASE_NAME = "Test";
    private static final String RESPONDENT_FIRST_NAME = "John";
    private static final String RESPONDENT_LAST_NAME = "Smith";
    public static final String EXPECTED_NAME = String.format("%s %s", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME);
    public static final List<Element<Child>> CHILDREN = wrapElements(mock(Child.class));

    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID).children1(CHILDREN).caseName(CASE_NAME).build();
    public static final String CHILD_LAST_NAME = "Jones";

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private NoticeOfChangeContentProvider underTest;

    @BeforeEach
    void init() {
        when(helper.getEldestChildLastName(CHILDREN)).thenReturn(CHILD_LAST_NAME);
    }

    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldBuildNotificationDataForSolicitorAccessGranted(String firstName, String lastName,
                                                              String expectedSalutation) {
        Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate
            = buildExpectedTemplate(expectedSalutation, EXPECTED_NAME);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondent))
            .isEqualTo(expectedTemplate);
    }

    @ParameterizedTest
    @MethodSource("solicitorNameSource")
    void shouldBuildNotificationDataForSolicitorAccessRevoked(String firstName, String lastName,
                                                              String expectedSalutation) {
        Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate
            = buildExpectedTemplate(expectedSalutation, EXPECTED_NAME);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondent))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildNotificationDataForSolicitorAccessRevokedWhenRespondentNameIsMissing() {
        Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .party(null)
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate
            = buildExpectedTemplate("Dear John Smith", EMPTY);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondent))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldReturnEmptyNameNameIfSolicitorMissing() {
        Respondent respondent = Respondent.builder()
            .solicitor(null)
            .party(null)
            .build();

        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate
            = buildExpectedTemplate("", EMPTY);

        assertThat(underTest.buildNoticeOfChangeRespondentSolicitorTemplate(CASE_DATA, respondent))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildNoticeOfChangeTemplateForThirdPartySolicitor() {
        NoticeOfChangeRespondentSolicitorTemplate expectedTemplate
            = buildExpectedTemplate("", EMPTY);

        assertThat(underTest.buildNoticeOfChangeThirdPartySolicitorTemplate(CASE_DATA))
            .isEqualTo(expectedTemplate);
    }

    private static Stream<Arguments> solicitorNameSource() {
        final String salutation = "Dear ";
        final String expectedLastName = salutation + RESPONDENT_LAST_NAME;
        final String expectedFirstName = salutation + RESPONDENT_FIRST_NAME;
        final String expectedFullName = salutation + RESPONDENT_FIRST_NAME + " " + RESPONDENT_LAST_NAME;

        return Stream.of(
            Arguments.of(null, RESPONDENT_LAST_NAME, expectedLastName),
            Arguments.of(EMPTY, RESPONDENT_LAST_NAME, expectedLastName),
            Arguments.of(RESPONDENT_FIRST_NAME, null, expectedFirstName),
            Arguments.of(RESPONDENT_FIRST_NAME, EMPTY, expectedFirstName),
            Arguments.of(null, null, ""),
            Arguments.of(EMPTY, EMPTY, ""),
            Arguments.of(RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME, expectedFullName)
        );
    }

    private NoticeOfChangeRespondentSolicitorTemplate buildExpectedTemplate(String expectedSalutation,
                                                                            String clientName) {
        return NoticeOfChangeRespondentSolicitorTemplate.builder()
            .salutation(expectedSalutation)
            .caseName(CASE_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseUrl("http://fake-url/cases/case-details/" + CASE_ID)
            .clientFullName(clientName)
            .childLastName(CHILD_LAST_NAME)
            .build();
    }
}
