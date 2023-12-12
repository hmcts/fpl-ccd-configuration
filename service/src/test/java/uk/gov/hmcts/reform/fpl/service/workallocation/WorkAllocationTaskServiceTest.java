package uk.gov.hmcts.reform.fpl.service.workallocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType.FAILED_PAYMENT;

@ExtendWith(MockitoExtension.class)
public class WorkAllocationTaskServiceTest {

    @Mock
    private CoreCaseDataService ccdService;

    @InjectMocks
    private WorkAllocationTaskService underTest;

    private static final long CASE_ID = 1L;
    private static final String WORK_ALLOCATION_DUMMY_EVENT = "create-work-allocation-task";
    private static final String WORK_ALLOCATION_DUMMY_CASE_FIELD = "lastCreatedWATask";

    @Test
    void shouldTriggerWorkAllocationDummyEvent() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.createWorkAllocationTask(caseData, FAILED_PAYMENT);

        verify(ccdService).triggerEvent(CASE_ID, WORK_ALLOCATION_DUMMY_EVENT, Map.of(
            WORK_ALLOCATION_DUMMY_CASE_FIELD, FAILED_PAYMENT
        ));
    }
}
