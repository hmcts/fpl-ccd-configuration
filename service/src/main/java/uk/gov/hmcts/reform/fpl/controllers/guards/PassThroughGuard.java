package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;

@Service
public class PassThroughGuard implements EventGuard {
    @Override
    public List<String> validate(CaseDetails caseDetails) {
        return Collections.emptyList();
    }
}
