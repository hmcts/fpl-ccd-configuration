package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.JUDICIAL_MESSAGES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JudicialMessageContentProvider.class, FixedTimeConfiguration.class})
class JudicialMessageContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private JudicialMessageContentProvider newJudicialMessageContentProvider;

    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);

    private static final String HEARING_CALLOUT = "hearing " + HEARING_DATE
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));

    @ParameterizedTest
    @MethodSource("urgency")
    void testMessageContentWithDifferentUrgency(String urgency, String expectedUrgency, YesNo hasUrgency) {
        CaseData caseData = buildCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency(urgency)
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .latestMessage("Please see latest C2")
            .hasUrgency(hasUrgency.getValue())
            .urgency(expectedUrgency)
            .callout("^Smith, 12345, " + HEARING_CALLOUT)
            .caseUrl(caseUrl(CASE_REFERENCE, JUDICIAL_MESSAGES))
            .respondentLastName("Smith")
            .hasApplication("No")
            .applicationType("")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);

    }

    @ParameterizedTest
    @MethodSource("application")
    void testMessageContentWithDifferentApplicationType(String applicationType, YesNo hasApplication) {
        CaseData caseData = buildCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("")
            .applicationType(applicationType)
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .latestMessage("Please see latest C2")
            .callout("^Smith, 12345, " + HEARING_CALLOUT)
            .caseUrl(caseUrl(CASE_REFERENCE, JUDICIAL_MESSAGES))
            .respondentLastName("Smith")
            .hasUrgency(NO.getValue())
            .urgency("")
            .hasApplication(hasApplication.getValue())
            .applicationType(applicationType)
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);

    }

    private static Stream<Arguments> urgency() {
        return Stream.of(
            Arguments.of("Very urgent", "Very urgent", YES),
            Arguments.of("", "", NO),
            Arguments.of(null, "", NO));
    }

    private static Stream<Arguments> application() {
        return Stream.of(
            Arguments.of("C100", YES),
            Arguments.of("", NO)
        );
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build()).build()))
            .hearingDetails(
                wrapElements(HearingBooking.builder().startDate(HEARING_DATE).build()))
            .build();
    }
}
