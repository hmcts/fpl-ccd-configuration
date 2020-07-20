package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Collections;
import java.util.List;

@Service
public class PassThroughValidator implements EventValidator {

    @Override
    public List<String> validate(CaseData caseData) {
        return Collections.emptyList();
    }
}
