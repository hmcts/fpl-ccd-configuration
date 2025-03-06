package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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

    final CaseData caseData = CaseData.builder()
        .id(10L)
        .state(State.OPEN)
        .isLocalAuthority(YES)
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
    void shouldUpdateTaskListWithIfLocalAuthority() {
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(false);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList.md").trim();

        verify(concurrencyHelper).submitEvent(
            any(),
            eq(caseData.getId()),
            eq(Map.of("taskList", expectedTaskList)));
    }

    @Test
    void shouldIncludeLanguageSelectionIfToggledOn() {
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(true);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskListWithLanguageRequirement.md").trim();

        verify(concurrencyHelper).submitEvent(any(),
            eq(caseData.getId()),
            eq(Map.of("taskList", expectedTaskList)));
    }

    @Test
    void shouldUpdateTaskListWithIfNotLocalAuthority() {
        final CaseData caseDataSolicitor = caseData.toBuilder()
            .isLocalAuthority(NO)
            .representativeType(RepresentativeType.RESPONDENT_SOLICITOR)
            .build();

        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(false);
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseDataSolicitor))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseDataSolicitor);

        String expectedTaskList = readString("fixtures/taskList-solicitor.md").trim();

        verify(concurrencyHelper).submitEvent(any(),
            eq(caseDataSolicitor.getId()),
            eq(Map.of("taskList", expectedTaskList)));
    }
}
