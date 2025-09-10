package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicantsDetailsUpdatedNotifyData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {ApplicantsDetailsUpdatedContentProvider.class})
class ApplicantsDetailsUpdatedContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private ApplicantsDetailsUpdatedContentProvider underTest;

    @Test
    void shouldReturnNotifyData_forLocalAuthority() {
        String respondentLastName = "Respondent";

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber(CASE_REFERENCE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(respondentLastName).build())
                .build()))
            .build();

        assertThat(underTest.getApplicantsDetailsUpdatedNotifyData(caseData))
            .isEqualTo(ApplicantsDetailsUpdatedNotifyData.builder()
                .familyManCaseNumber(CASE_REFERENCE)
                .firstRespondentLastNameOrLaName(respondentLastName)
                .caseUrl(caseUrl(CASE_REFERENCE)).build());
    }

    @Test
    void shouldReturnNotifyData_forThirdPary() {
        String respondentFirstName = "Example Local Authority";

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber(CASE_REFERENCE)
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName(respondentFirstName).lastName(null).build())
                .build()))
            .build();

        assertThat(underTest.getApplicantsDetailsUpdatedNotifyData(caseData))
            .isEqualTo(ApplicantsDetailsUpdatedNotifyData.builder()
                .familyManCaseNumber(CASE_REFERENCE)
                .firstRespondentLastNameOrLaName(respondentFirstName)
                .caseUrl(caseUrl(CASE_REFERENCE)).build());
    }
}
