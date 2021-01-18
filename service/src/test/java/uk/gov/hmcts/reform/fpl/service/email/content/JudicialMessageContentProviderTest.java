package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JudicialMessageContentProvider.class, FixedTimeConfiguration.class})
class JudicialMessageContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private JudicialMessageContentProvider newJudicialMessageContentProvider;

    @Autowired
    Time time;

    //Tech debt - maybe mock callout instead of setting date to arbitrary far date in future
    private static final LocalDateTime HEARING_START_DATE = LocalDateTime.of(3000, 1, 1, 11, 11, 11);

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = buildCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("Needed asap")
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .latestMessage("Please see latest C2")
            .hasUrgency(YES.getValue())
            .urgency("Needed asap")
            .callout("^Smith, 12345, hearing 1 Jan 3000")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .usingRecursiveComparison().isEqualTo(expectedTemplate);
    }

    @Test
    void shouldSetUrgencyToNoWhenNotPresentOnJudicialMessageMetaData() {
        CaseData caseData = buildCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .latestMessage("Please see latest C2")
            .hasUrgency(NO.getValue())
            .urgency("")
            .callout("^Smith, 12345, hearing 1 Jan 3000")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldSetUrgencyToNoWhenNotUrgencyDefinedAsEmptyStringOnJudicialMessageMetaData() {
        CaseData caseData = buildCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("")
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .latestMessage("Please see latest C2")
            .hasUrgency(NO.getValue())
            .urgency("")
            .callout("^Smith, 12345, hearing 1 Jan 3000")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build()).build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_START_DATE)).build()))
            .build();
    }
}
