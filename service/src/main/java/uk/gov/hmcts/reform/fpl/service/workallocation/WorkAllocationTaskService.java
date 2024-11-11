package uk.gov.hmcts.reform.fpl.service.workallocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.workallocation.ClientContext;
import uk.gov.hmcts.reform.fpl.model.workallocation.ClientContextHeader;
import uk.gov.hmcts.reform.fpl.model.workallocation.UserTask;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WorkAllocationTaskService {

    private static final String WORK_ALLOCATION_DUMMY_EVENT = "create-work-allocation-task";
    private static final String WORK_ALLOCATION_DUMMY_CASE_FIELD = "lastCreatedWATask";

    private final CoreCaseDataService coreCaseDataService;

    public void createWorkAllocationTask(CaseData caseData, WorkAllocationTaskType taskType) {
        log.info("Creating work allocation task {} on case {}", taskType.name(), caseData.getId());
        coreCaseDataService.performPostSubmitCallback(caseData.getId(), WORK_ALLOCATION_DUMMY_EVENT,
            caseDetails -> Map.of(WORK_ALLOCATION_DUMMY_CASE_FIELD, taskType));
    }

    public Optional<UserTask> getUserTask(ClientContextHeader header) {
        return Optional.ofNullable(header)
            .map(ClientContextHeader::getClientContext)
            .map(ClientContext::getUserTask);
    }

}
