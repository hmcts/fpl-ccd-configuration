package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;

@ContextConfiguration(classes = {NewJudicialMessageContentProvider.class})
class NewJudicialMessageContentProviderTest extends AbstractEmailContentProviderTest {
    @Autowired
    private NewJudicialMessageContentProvider newJudicialMessageContentProvider;

    @Test
    void createTemplateWithExpectedParameters() {
        CaseData caseData = populatedCaseData();

        caseData = caseData.toBuilder()
            .judicialMessageNote("Please see latest C2")
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .recipient("paulStuart@fpla.com")
                .sender("robertDunlop@fpla.com")
                .urgency("Needed asap")
                .build())
            .build();

        NewJudicialMessageTemplate expectedTemplate = NewJudicialMessageTemplate.builder()
            .sender("robertDunlop@fpla.com")
            .note("Please see latest C2")
            .hasUrgency(YES.getValue())
            .urgency("Needed asap")
            .callout("^Smith, 12345, hearing 1 Jan 2020")
            .caseUrl(caseUrl(CASE_REFERENCE, "JudicialMessagesTab"))
            .respondentLastName("Smith")
            .build();

        assertThat(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldNotSetUrgencyWhenNotPresentOnJudicialMessageMetaData() {
        CaseData caseData = populatedCaseData();

        caseData = caseData.toBuilder()
            .judicialMessageNote("Please see latest C2")
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .recipient("paulStuart@fpla.com")
                .sender("robertDunlop@fpla.com")
                .build())
            .build();

        NewJudicialMessageTemplate template
            = newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData);

        assertThat(template.getHasUrgency()).isNull();
        assertThat(template.getUrgency()).isNull();
    }
}
