package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Event {

    ORDERS_SOUGHT("ordersNeeded", "Orders and directions sought", "Gorchmynion a chyfarwyddiadau a geisir"),
    HEARING_URGENCY("hearingNeeded", "Hearing urgency", "Gwrandawiad brys"),
    GROUNDS("enterGrounds", "Grounds for the application", "Seiliau'r cais"),
    RISK_AND_HARM("enterRiskHarm", "Risk and harm to children", "Risg a niwed i blant"),
    FACTORS_AFFECTING_PARENTING("enterParentingFactors", "Factors affecting parenting",
        "Ffactorau sy’n effeithio ar rianta"),
    ////TO DO remove when toggling on FPLA-768
    DOCUMENTS("uploadDocuments", "Upload documents", "Uwchlwytho dogfennau"),
    ORGANISATION_DETAILS("enterApplicant", "Applicant's details", "Manylion y ceisydd"),
    LOCAL_AUTHORITY_DETAILS("enterLocalAuthority", "Applicant's details", "Manylion y ceisydd"),
    CHILDREN("enterChildren", "Child's details", "Manylion y plentyn"),
    RESPONDENTS("enterRespondents", "Respondents' details", "Manylion yr atebydd"),
    ALLOCATION_PROPOSAL("otherProposal", "Allocation proposal", "Cynnig dyrannu"),
    OTHER_PROCEEDINGS("otherProceedings", "Other proceedings", "Achosion eraill"),
    INTERNATIONAL_ELEMENT("enterInternationalElement", "International element", "Elfen ryngwladol"),
    OTHERS("enterOthers", "Other people to be given notice", "Pobl eraill i gael hysbysiad"),
    COURT_SERVICES("attendingHearing", "Court services", "Gwasanaethau llys"),
    LANGUAGE_REQUIREMENTS("languageSelection", "Welsh language requirements", "Gofynion iaith Gymraeg"),
    SUBMIT_APPLICATION("submitApplication", "Submit application", "Cyflwyno'r cais"),
    CASE_NAME("changeCaseName", "Change case name", "Newid enw’r achos"),
    APPLICATION_DOCUMENTS("uploadDocuments", "Upload documents", "Uwchlwytho dogfennau"),
    SELECT_COURT("selectCourt", "Select court to issue", "Dewiswch lys ar gyfer cylfwyno"),
    C1_WITH_SUPPLEMENT("enterC1WithSupplement", "C1 with Supplement", "C1 gydag atodiad"),
    // Following events not used in case creation
    JUDICIAL_GATEKEEPNIG("addGatekeepingOrder", "Judicial Gatekeeping", "Judicial Gatekeeping"),
    ADD_URGENT_DIRECTIONS("addUrgentDirections", "Add urgent directions", "Add urgent directions");

    private final String id;
    private final String name;
    private final String welshName;

    @JsonIgnore
    public String getNameInLang(boolean welsh) {
        return welsh ? welshName : name;
    }

}
