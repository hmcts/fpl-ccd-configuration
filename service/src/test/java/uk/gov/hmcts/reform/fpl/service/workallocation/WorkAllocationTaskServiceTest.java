package uk.gov.hmcts.reform.fpl.service.workallocation;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WorkAllocationTaskServiceTest {

    @Mock
    private CoreCaseDataService ccdService;

    @Captor
    private ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> captor;

    @InjectMocks
    private WorkAllocationTaskService underTest;

    private static final long CASE_ID = 1L;
    private static final String WORK_ALLOCATION_DUMMY_EVENT = "create-work-allocation-task";
    private static final String WORK_ALLOCATION_DUMMY_CASE_FIELD = "lastCreatedWATask";

    @ParameterizedTest
    @EnumSource(value = WorkAllocationTaskType.class)
    void shouldTriggerWorkAllocationDummyEvent(WorkAllocationTaskType taskType) {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.createWorkAllocationTask(caseData, taskType);

        verify(ccdService).performPostSubmitCallback(eq(CASE_ID), eq(WORK_ALLOCATION_DUMMY_EVENT), captor.capture());
        assertThat(captor.getValue().apply(CaseDetails.builder().build()))
            .isEqualTo(Map.of(WORK_ALLOCATION_DUMMY_CASE_FIELD, taskType));
    }
}
