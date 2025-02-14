package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Event {

    ORDERS_SOUGHT("ordersNeeded", "Orders and directions sought"),
    HEARING_URGENCY("hearingNeeded", "Hearing urgency"),
    GROUNDS("enterGrounds", "Grounds for the application"),
    RISK_AND_HARM("enterRiskHarm", "Risk and harm to children"),
    FACTORS_AFFECTING_PARENTING("enterParentingFactors", "Factors affecting parenting"),
    ////TO DO remove when toggling on FPLA-768
    DOCUMENTS("uploadDocuments", "Upload documents"),
    APPLICANT_DETAILS_LA("enterApplicantDetailsLA", "Applicant's details"),
    APPLICANT_DETAILS_SOLICITOR("TODO", "Applicant's details"),
    CHILDREN("enterChildren", "Child's details"),
    RESPONDENTS("enterRespondents", "Respondents' details"),
    ALLOCATION_PROPOSAL("otherProposal", "Allocation proposal"),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings"),
    INTERNATIONAL_ELEMENT("enterInternationalElement", "International element"),
    OTHERS("enterOthers", "Other people in the case"),
    COURT_SERVICES("attendingHearing", "Court services needed"),
    LANGUAGE_REQUIREMENTS("languageSelection", "Welsh language requirements"),
    SUBMIT_APPLICATION("submitApplication", "Submit application"),
    CASE_NAME("changeCaseName", "Change case name"),
    APPLICATION_DOCUMENTS("uploadDocuments", "Upload documents"),
    SELECT_COURT("selectCourt", "Select court to issue"),
    ADD_URGENT_DIRECTIONS("addUrgentDirections", "Add urgent directions"),
    JUDICIAL_GATEKEEPNIG("addGatekeepingOrder", "Judicial Gatekeeping"),
    C1_WITH_SUPPLEMENT("enterC1WithSupplement", "C1 with Supplement");

    private final String id;
    private final String name;

}
