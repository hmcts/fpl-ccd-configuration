package uk.gov.hmcts.reform.fpl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Roles;
import uk.gov.hmcts.reform.fpl.enums.State;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.FplEvent.ALLOCATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.FplEvent.AMEND_CHILDREN;
import static uk.gov.hmcts.reform.fpl.FplEvent.AMEND_OTHERS;
import static uk.gov.hmcts.reform.fpl.FplEvent.CREATE_ORDER;
import static uk.gov.hmcts.reform.fpl.FplEvent.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.FplEvent.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.FplEvent.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.UPLOAD_C2;
import static uk.gov.hmcts.reform.fpl.FplEvent.UPLOAD_DOCUMENTS_AFTER_SUBMISSION;
import static uk.gov.hmcts.reform.fpl.enums.Roles.ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

@Service
public class AllEvents {


    public List<FplEvent> getOptionalEvents(State state, Roles roles) {
        if (roles == ADMIN) {
            if (state == SUBMITTED) {
                return List.of(CREATE_ORDER, HEARING_DETAILS, INTERNATIONAL_ELEMENT, ALLOCATE_JUDGE, UPLOAD_DOCUMENTS_AFTER_SUBMISSION, UPLOAD_C2,
                    AMEND_CHILDREN, RESPONDENTS, AMEND_OTHERS);
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public List<FplEvent> getMandatoryEvents(State state, Roles roles) {
        if (roles == ADMIN) {
            if (state == SUBMITTED) {
                return List.of(FplEvent.ADD_CASE_NUMBER, FplEvent.NOTIFY_GATEKEEPER);
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

}
