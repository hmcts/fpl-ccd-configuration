package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.JudicialMessageReplyTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.JUDICIAL_MESSAGES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {JudicialMessageReplyContentProvider.class})
class JudicialMessageReplyContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private JudicialMessageReplyContentProvider judicialMessageReplyContentProvider;

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = populatedCaseData();

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .latestMessage("Please see latest C2")
            .recipient("paulStuart@fpla.com")
            .sender("robertDunlop@fpla.com")
            .urgency("Needed asap")
            .build();

        JudicialMessageReplyTemplate expectedTemplate = JudicialMessageReplyTemplate.builder()
            .latestMessage("Please see latest C2")
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, JUDICIAL_MESSAGES))
            .respondentLastName("Smith")
            .build();

        assertThat(judicialMessageReplyContentProvider.buildJudicialMessageReplyTemplate(caseData, judicialMessage))
            .isEqualTo(expectedTemplate);
    }
}
