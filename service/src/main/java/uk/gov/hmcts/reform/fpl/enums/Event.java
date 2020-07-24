package uk.gov.hmcts.reform.fpl.enums;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

public enum Event {

    DRAFT_CASE_MANAGEMENT_ORDER("draftCMO","Draft CMO", List.of(PREPARE_FOR_HEARING)),
    ACTION_CASE_MANAGEMENT_ORDER("actionCMO","Action CMO", List.of(PREPARE_FOR_HEARING)),

    ORDERS_NEEDED("ordersNeeded", "Orders and directions needed", List.of(OPEN)),
    HEARING_NEEDED("hearingNeeded", "Hearing needed", List.of(OPEN, RETURNED)),
    GROUNDS("enterGrounds", "Grounds for the application", List.of(OPEN, RETURNED)),
    RISK_AND_HARM("enterRiskHarm", "Risk and harm to children", List.of(OPEN, RETURNED)),
    FACTORS_AFFECTING_PARENTING("enterParentingFactors", "Factors affecting parenting", List.of(OPEN, RETURNED)),
    DOCUMENTS("uploadDocuments", "Documents", List.of(OPEN, SUBMITTED, GATEKEEPING, PREPARE_FOR_HEARING, RETURNED)),
    APPLICANT("enterApplicant", "Applicant", List.of(OPEN, RETURNED)),
    ENTER_CHILDREN("enterChildren", "Children", List.of(OPEN, RETURNED)),
    RESPONDENTS("enterRespondents", "Respondents", List.of(OPEN, RETURNED)),
    ALLOCATION_PROPOSAL("otherProposal", "Allocation proposal", List.of(OPEN, RETURNED)),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings", List.of(OPEN, RETURNED)),
    INTERNATIONAL_ELEMENT("enterInternationalElement", "International element", List.of(OPEN, RETURNED)),
    ENTER_OTHERS("enterOthers", "Others to be given notice", List.of(OPEN, RETURNED)),
    ATTENDING_THE_HEARING("attendingHearing", "Attending the hearing", List.of(OPEN, RETURNED)),
    SUBMIT_APPLICATION("submitApplication", "Submit application", List.of(OPEN, RETURNED)),
    CASE_NAME("changeCaseName", "Change case name", List.of(OPEN, RETURNED));

    String id;
    String name;
    List<State> states;

    Event(String id, String name, List<State> states) {
        this.id = id;
        this.name = name;
        this.states = states;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<State> getStates() {
        return states;
    }

    public static List<Event> eventsInState(State state) {
        return Stream.of(Event.values())
            .filter(event -> event.getStates().contains(state))
            .collect(Collectors.toList());
    }
}
