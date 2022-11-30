package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RemovableType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.RemovedApplicationForm;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationFormRemovedNotifyData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {ApplicationFormRemovedEmailContentProvider.class})
class ApplicationFormRemovedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    public static final String CASE_NAME = "Test Case";
    public static final Long CASE_ID = 12345L;
    public static final String REMOVAL_REASON = "Confidential information disclosed.";
    public static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
        .build();

    @Autowired
    private ApplicationFormRemovedEmailContentProvider underTest;

    @Test
    void shouldReturnExpectedMapWithGivenCaseDetails() {
        CaseData caseData = buildCaseData();

        ApplicationFormRemovedNotifyData expectedParameters = ApplicationFormRemovedNotifyData.builder()
            .caseName(CASE_NAME)
            .removalReason(REMOVAL_REASON)
            .familyManCaseNumber(CASE_REFERENCE)
            .caseUrl(caseUrl(CASE_ID.toString()))
            .lastName(RESPONDENT.getParty().getLastName())
            .build();

        ApplicationFormRemovedNotifyData actualParameters = underTest.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_REFERENCE)
            .caseName(CASE_NAME)
            .removalToolData(RemovalToolData.builder()
                .hiddenApplicationForm(RemovedApplicationForm.builder()
                    .removalReason(REMOVAL_REASON)
                    .build())
                .removableType(RemovableType.APPLICATION)
                .build())
            .respondents1(wrapElements(RESPONDENT))
            .build();
    }
}
