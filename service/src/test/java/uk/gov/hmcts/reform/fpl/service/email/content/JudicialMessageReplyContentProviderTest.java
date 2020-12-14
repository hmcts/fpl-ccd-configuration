package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageReplyTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {JudicialMessageReplyContentProvider.class})
class JudicialMessageReplyContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private JudicialMessageReplyContentProvider newJudicialMessageReplyContentProvider;

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = populatedCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("Needed asap")
            .build();

        NewJudicialMessageReplyTemplate expectedTemplate = NewJudicialMessageReplyTemplate.builder()
            .latestMessage("Please see latest C2")
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageReplyContentProvider.buildNewJudicialMessageReplyTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }
}
