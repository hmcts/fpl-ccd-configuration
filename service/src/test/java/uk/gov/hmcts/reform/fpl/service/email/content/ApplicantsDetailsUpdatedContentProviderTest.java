package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicantsDetailsUpdatedNotifyData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {ApplicantsDetailsUpdatedContentProvider.class})
class ApplicantsDetailsUpdatedContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String RESPONDENT_LAST_NAME = "Respondent";
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .familyManCaseNumber(CASE_REFERENCE)
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build())
            .build()))
        .build();

    @Autowired
    private ApplicantsDetailsUpdatedContentProvider underTest;

    @Test
    void shouldReturnNotifyData() {
        assertThat(underTest.getApplicantsDetailsUpdatedNotifyData(CASE_DATA))
            .isEqualTo(ApplicantsDetailsUpdatedNotifyData.builder()
                .familyManCaseNumber(CASE_REFERENCE)
                .firstRespondentLastName(RESPONDENT_LAST_NAME)
                .caseUrl(caseUrl(CASE_REFERENCE)).build());
    }
}
