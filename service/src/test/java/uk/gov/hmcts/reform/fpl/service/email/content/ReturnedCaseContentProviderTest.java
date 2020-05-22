package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.notify.returnedcase.ReturnedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {ReturnedCaseContentProvider.class, LookupTestConfig.class})
class ReturnedCaseContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private ReturnedCaseContentProvider returnedCaseContentProvider;

    @Test
    void shouldBuildReturnedExpectedTemplateWithCompleteCaseDetails() {
        ReturnedCaseTemplate expectedReturnCaseTemplate = new ReturnedCaseTemplate();

        expectedReturnCaseTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        expectedReturnCaseTemplate.setFamilyManCaseNumber("12345");
        expectedReturnCaseTemplate.setRespondentLastName("Smith");
        expectedReturnCaseTemplate.setRespondentFullName("Paul Smith");
        expectedReturnCaseTemplate.setReturnedReasons("Application incomplete, clarification needed");
        expectedReturnCaseTemplate.setReturnedNote("Missing children details");
        expectedReturnCaseTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));

        assertThat(returnedCaseContentProvider.buildNotificationParameters(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(expectedReturnCaseTemplate);
    }

    @Test
    void shouldBuildReturnedExpectedTemplateWithInCompleteCaseDetails() {
        ReturnedCaseTemplate expectedReturnCaseTemplate = new ReturnedCaseTemplate();

        expectedReturnCaseTemplate.setLocalAuthority(LOCAL_AUTHORITY_NAME);
        expectedReturnCaseTemplate.setFamilyManCaseNumber("");
        expectedReturnCaseTemplate.setRespondentLastName("Wilson");
        expectedReturnCaseTemplate.setRespondentFullName("Tim Wilson");
        expectedReturnCaseTemplate.setReturnedReasons("Application incomplete");
        expectedReturnCaseTemplate.setReturnedNote("Missing details");
        expectedReturnCaseTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));

        assertThat(returnedCaseContentProvider.buildNotificationParameters(buildInCompleteCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualToComparingFieldByField(expectedReturnCaseTemplate);
    }

    private CaseDetails buildInCompleteCaseDetails() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Tim")
                .lastName("Wilson")
                .build())
            .build();

        return CaseDetails.builder()
            .id(12345L)
            .data(ImmutableMap.of(
                "respondents1", wrapElements(respondent),
                RETURN_APPLICATION, ReturnApplication.builder()
                    .note("Missing details")
                    .reason(List.of(INCOMPLETE))
                    .build()))
            .build();
    }
}
