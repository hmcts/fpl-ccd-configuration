package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class TaskListControllerSubmittedTest extends AbstractCallbackTest {

    TaskListControllerSubmittedTest() {
        super("update-task-list");
    }

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    final CaseData caseData = CaseData.builder()
        .id(10L)
        .state(State.OPEN)
        .build();

    @BeforeEach
    void setup() {
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());
    }

    @Test
    void shouldUpdateTaskListWithAdditionalContactsToggledOff() {
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(10L)
            .state(State.OPEN)
            .build();

        when(featureToggleService.isApplicantAdditionalContactsEnabled()).thenReturn(false);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList-legacyApplicant.md").trim();

        verify(concurrencyHelper).submitEvent(any(),
            eq(caseData.getId()),
            dataCaptor.capture());

        assertThat(dataCaptor.getValue()).extracting("taskList").isEqualTo(expectedTaskList);
    }

    @Test
    void shouldUpdateTaskListWithAdditionalContactsToggledOn() {
        when(featureToggleService.isApplicantAdditionalContactsEnabled()).thenReturn(true);
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(false);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList.md").trim();

        verify(concurrencyHelper).submitEvent(
            any(),
            eq(caseData.getId()),
            dataCaptor.capture());
        assertThat(dataCaptor.getValue()).extracting("taskList").isEqualTo(expectedTaskList);

    }

    @Test
    void shouldIncludeLanguageSelectionIfToggledOn() {
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(true);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskListWithLanguageRequirement.md").trim();

        verify(concurrencyHelper).submitEvent(any(),
            eq(caseData.getId()),
            dataCaptor.capture());
        assertThat(dataCaptor.getValue()).extracting("taskList").isEqualTo(expectedTaskList);
    }
}
