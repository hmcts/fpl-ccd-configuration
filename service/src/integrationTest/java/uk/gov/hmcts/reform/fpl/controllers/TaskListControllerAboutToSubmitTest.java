package uk.gov.hmcts.reform.fpl.controllers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class TaskListControllerAboutToSubmitTest extends AbstractCallbackTest {

    TaskListControllerAboutToSubmitTest() {
        super("update-task-list");
    }

    @Test
    void shouldPopulateUpdatedCaseNameToGlobalSearchCaseNames() {
        Map<String, Object> caseDetails = postAboutToSubmitEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("Updated CaseName")
            .build()).getData();

        Assertions.assertThat(caseDetails.get("caseNameHmctsRestricted")).isEqualTo("Updated CaseName");
        Assertions.assertThat(caseDetails.get("caseNameHmctsInternal")).isEqualTo("Updated CaseName");
        Assertions.assertThat(caseDetails.get("caseNamePublic")).isEqualTo("Updated CaseName");

    }
}
