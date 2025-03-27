package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_LA;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_THIRD_PARTY;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_STARTED;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventsChecker eventsChecker;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventsChecker.isCompleted(event, caseData)) {
            return eventsChecker.completedState(event);
        }

        if (eventsChecker.isInProgress(event, caseData)) {
            return IN_PROGRESS;
        }

        if (!eventsChecker.isAvailable(event, caseData)) {
            return NOT_AVAILABLE;
        }

        return NOT_STARTED;
    }

    private List<Event> getEvents(CaseData caseData) {

        // Core Events for all combinations of C110a + C1 apps
        final List<Event> events = new ArrayList<>(List.of(
            ORDERS_SOUGHT,
            caseData.checkIfCaseIsSubmittedByLA() ? APPLICANT_DETAILS_LA : APPLICANT_DETAILS_THIRD_PARTY,
            CHILDREN,
            RESPONDENTS,
            OTHER_PROCEEDINGS,
            OTHERS,
            COURT_SERVICES,
            SUBMIT_APPLICATION,
            CASE_NAME,
            APPLICATION_DOCUMENTS,
            HEARING_URGENCY,
            ALLOCATION_PROPOSAL
        ));

        if (YES.equals(caseData.getMultiCourts())) {
            events.add(SELECT_COURT);
        }

        // C1s and C110a's (except SAO and DoC)
        if (!caseData.isSecureAccommodationOrderType()
                && !caseData.isDischargeOfCareApplication()
                && !caseData.isRefuseContactWithChildApplication()
                && !caseData.isContactWithChildInCareApplication()) {
            events.add(RISK_AND_HARM);
        }

        // C1s and C110a's (except DoC)
        if (!caseData.isDischargeOfCareApplication()) {
            events.add(GROUNDS);
        }

        // C110a's only
        if (!caseData.isC1Application()) {
            events.add(INTERNATIONAL_ELEMENT);
            events.add(LANGUAGE_REQUIREMENTS);
            if (isNotEmpty(caseData.getOrders()) && isNotEmpty(caseData.getOrders().getOrderType())) {
                events.add(C1_WITH_SUPPLEMENT);
            }
        }

        return events;
    }

    public Map<Event, String> getTaskHints(CaseData caseData) {
        Map<Event, String> taskHintsMap = new HashMap<>();
        if (caseData.isC1Application() && !caseData.isRefuseContactWithChildApplication()) {
            taskHintsMap.put(HEARING_URGENCY, "Optional for C1 applications");
        }
        return taskHintsMap;
    }
}
