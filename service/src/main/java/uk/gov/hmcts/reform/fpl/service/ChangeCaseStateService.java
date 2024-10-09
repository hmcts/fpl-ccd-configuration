package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.controllers.RecordFinalDecisionsController.CLOSE_CASE_TAB_FIELD;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;

@Service
public class ChangeCaseStateService {
    public static final String LABEL_CONTENT = "Do you want to change the case state to %s?";

    public Map<String, Object> initialiseEventFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (!isInClosedState(caseData)) {
            State availableNextState = getNextState(caseData.getState());
            data.put("nextStateLabelContent", String.format(LABEL_CONTENT,
                availableNextState.getLabel().toLowerCase()));
        }

        return data;
    }

    public Map<String, Object> updateCaseState(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (isInClosedState(caseData)) {
            data.putAll(migrateClosedCase(caseData));
        } else if (YesNo.fromString(caseData.getConfirmChangeState()) == YesNo.YES) {
            data.put("state", getNextState(caseData.getState()));
        }

        return data;
    }

    private Map<String, Object> migrateClosedCase(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        data.put(CLOSE_CASE_TAB_FIELD, null);
        data.put("state", caseData.getClosedStateRadioList());

        return data;
    }

    private State getNextState(State state) {
        return switch (state) {
            case GATEKEEPING, GATEKEEPING_LISTING, FINAL_HEARING -> CASE_MANAGEMENT;
            case CASE_MANAGEMENT -> FINAL_HEARING;
            default -> throw new IllegalStateException("Should not be able to change from: " + state);
        };
    }

    private boolean isInClosedState(CaseData caseData) {
        return CLOSED.equals(caseData.getState());
    }
}
