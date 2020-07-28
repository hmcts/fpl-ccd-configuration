package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

@Getter
public enum Event {

    DRAFT_CASE_MANAGEMENT_ORDER("draftCMO", "Draft CMO", List.of(PREPARE_FOR_HEARING)),
    ACTION_CASE_MANAGEMENT_ORDER("actionCMO", "Action CMO", List.of(PREPARE_FOR_HEARING)),

    ORDERS_SOUGHT("ordersNeeded", "Orders and directions sought", List.of(OPEN)),
    HEARING_URGENCY("hearingNeeded", "Hearing urgency", List.of(OPEN, RETURNED)),
    GROUNDS("enterGrounds", "Grounds for the application", List.of(OPEN, RETURNED)),
    RISK_AND_HARM("enterRiskHarm", "Risk and harm to children", List.of(OPEN, RETURNED)),
    FACTORS_AFFECTING_PARENTING("enterParentingFactors", "Factors affecting parenting", List.of(OPEN, RETURNED)),
    DOCUMENTS("uploadDocuments", "Upload documents",
        List.of(OPEN, SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, RETURNED)),
    ORGANISATION_DETAILS("enterApplicant", "Your organisation's details", List.of(OPEN, RETURNED)),
    CHILDREN("enterChildren", "Child's details", List.of(OPEN, RETURNED)),
    RESPONDENTS("enterRespondents", "Respondent's details", List.of(OPEN, RETURNED)),
    ALLOCATION_PROPOSAL("otherProposal", "Allocation proposal", List.of(OPEN, RETURNED)),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings", List.of(OPEN, RETURNED)),
    INTERNATIONAL_ELEMENT("enterInternationalElement", "International element", List.of(OPEN, RETURNED)),
    OTHERS("enterOthers", "Other people in the case", List.of(OPEN, RETURNED)),
    COURT_SERVICES("attendingHearing", "Court services needed", List.of(OPEN, RETURNED)),
    SUBMIT_APPLICATION("submitApplication", "Submit application", List.of(OPEN, RETURNED)),
    CASE_NAME("changeCaseName", "Change case name", List.of(OPEN, RETURNED));

    private final String id;
    private String name;
    private List<State> states;

    Event(String id, String name, List<State> states) {
        this.id = id;
        this.name = name;
        this.states = states;
    }

    public static List<Event> eventsInState(State state) {
        return Stream.of(Event.values())
            .filter(event -> event.getStates().contains(state))
            .collect(Collectors.toList());
    }
}
