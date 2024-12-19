package uk.gov.hmcts.reform.fpl.service.ttl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TTLService {

    private static final String TTL_DUMMY_EVENT = "trigger-ttl-increment";

    private final CoreCaseDataService coreCaseDataService;

    public void triggerTimeToLiveIncrement(CaseData caseData) {
        log.info("Increment TTL 180 days on case {}", caseData.getId());
        coreCaseDataService.performPostSubmitCallbackWithoutChange(caseData.getId(), TTL_DUMMY_EVENT);
    }

}
