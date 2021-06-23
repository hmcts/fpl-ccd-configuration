package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.JudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.JUDICIAL_MESSAGES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {JudicialMessageReplyContentProvider.class, FixedTimeConfiguration.class})
class JudicialMessageReplyContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);
    private static final String HEARING_CALLOUT = "hearing " + HEARING_DATE
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));
    private static final String LAST_NAME = "Jones";
    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private JudicialMessageReplyContentProvider underTest;

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(LAST_NAME).build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .build();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("Needed asap")
            .applicationType("C2 (With notice)")
            .build();

        JudicialMessageReplyTemplate expectedTemplate = JudicialMessageReplyTemplate.builder()
            .latestMessage("Please see latest C2")
            .callout("^Smith, 12345, " + HEARING_CALLOUT)
            .caseUrl(caseUrl(CASE_REFERENCE, JUDICIAL_MESSAGES))
            .lastName(LAST_NAME)
            .hasApplication(YES.getValue())
            .applicationType("C2 (With notice)")
            .build();

        when(helper.getSubjectLineLastName(caseData)).thenReturn(LAST_NAME);

        assertThat(underTest.buildJudicialMessageReplyTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }
}
