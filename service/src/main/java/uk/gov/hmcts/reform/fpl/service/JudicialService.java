package uk.gov.hmcts.reform.fpl.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialService {

    private final CaseAccessService caseAccessService;

    public void assignAllocatedJudge(Long caseId, String userId) {
        caseAccessService.grantJudgeCaseRole(caseId, userId, ALLOCATED_JUDGE);
    }

    public void assignHearingJudge(Long caseId, String userId) {
        caseAccessService.grantJudgeCaseRole(caseId, userId, HEARING_JUDGE);
    }

}
