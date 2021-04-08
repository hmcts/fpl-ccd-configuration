package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {RespondentSolicitorContentProvider.class, LookupTestConfig.class})
class RespondentSolicitorContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private RespondentSolicitorContentProvider underTest;

    @Test
    void shouldReturnExpectedMapWithRepresentativeNameAndLocalAuthorityName() {
        RespondentSolicitorTemplate respondentSolicitorTemplate = RespondentSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .representativeName("John Smith")
            .build();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName("John").lastName("Smith")
            .organisation(Organisation.builder().organisationID("123").build())
            .build();
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder().solicitor(respondentSolicitor).build());

        CaseData caseData = populatedCaseData(Map.of("respondents1", respondents));

        assertThat(underTest.buildRespondentSolicitorSubmissionNotification(caseData, respondentSolicitor))
            .usingRecursiveComparison().isEqualTo(respondentSolicitorTemplate);
    }
}
