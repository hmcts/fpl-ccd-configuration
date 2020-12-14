package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {JudicialMessageContentProvider.class})
class JudicialMessageContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private JudicialMessageContentProvider newJudicialMessageContentProvider;

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = populatedCaseData();

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
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldSetUrgencyToNoWhenNotPresentOnJudicialMessageMetaData() {
        CaseData caseData = populatedCaseData();

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
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldSetUrgencyToNoWhenNotUrgencyDefinedAsEmptyStringOnJudicialMessageMetaData() {
        CaseData caseData = populatedCaseData();

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
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }
}
