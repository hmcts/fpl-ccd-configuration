package uk.gov.hmcts.reform.fpl;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.Roles;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.function.Predicate;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Roles.ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.Roles.LA_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

public enum FplEvent {

    ADD_CASE_NUMBER("addFamilyManCaseNumber", "Add case number", List.of(SUBMITTED), List.of(ADMIN), caseData -> StringUtils.isNotBlank(caseData.getFamilyManCaseNumber())),
    NOTIFY_GATEKEEPER("notifyGatekeeper", "Notify gatekeeper", List.of(SUBMITTED), List.of(ADMIN), caseData -> isNotEmpty(caseData.getGatekeeperEmails())),
    CREATE_ORDER("createOrder", "Create an order", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getOrderCollection())),
    HEARING_DETAILS("hearingBookingDetails", "Add hearing details", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getHearingDetails())),
    INTERNATIONAL_ELEMENT("amendInternationalElement", "International elements", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getInternationalElement())),
    ALLOCATE_JUDGE("allocatedJudge", "Allocate judge", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getAllocatedJudge())),
    UPLOAD_DOCUMENTS_AFTER_SUBMISSION("uploadDocumentsAfterSubmission", "Documents", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getThresholdDocument())),
    UPLOAD_C2("uploadC2", "Upload C2", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getAllocatedJudge())),
    AMEND_CHILDREN("amendChildren", "Children", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getAllChildren())),
    RESPONDENTS("enterRespondents", "Respondents", List.of(SUBMITTED), List.of(ADMIN, LA_SOLICITOR), c -> isNotEmpty(c.getAllRespondents())),
    AMEND_OTHERS("amendOthers", "Others to be given notice", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getOthers())),


    SUBMIT_APPLICATION("submitApplication", "Submit application", List.of(SUBMITTED), List.of(ADMIN), c -> isNotEmpty(c.getDateSubmitted())),
    ENTER_CHILDREN("enterChildren", "Enter children", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getAllChildren())),
    APPLICANT("enterApplicant", "Enter applicant", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getApplicants())),
    ORDERS_NEEDED("ordersNeeded", "Orders and direction needed", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getOrders())),
    GROUNDS("enterGrounds", "Grounds for application", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getGrounds())),
    HEARING_NEEDED("hearingNeeded", "Hearing needed", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getHearing())),
    DOCUMENTS("uploadDocuments", "Documents", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getThresholdDocument())),
    CASENAME("changeCaseName", "Case name", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getCaseName())),
    ALLOCATION_PROPOSAL("otherProposal", "Allocation proposal", List.of(OPEN), List.of(LA_SOLICITOR), c -> isNotEmpty(c.getAllocationProposal()));

    String id;
    String name;
    List<State> states;
    List<Roles> roles;
    Predicate<CaseData> completedPredicate;

    FplEvent(String id, String name, List<State> states, List<Roles> roles, Predicate<CaseData> completedPredicate) {
        this.id = id;
        this.name = name;
        this.states = states;
        this.roles = roles;
        this.completedPredicate = completedPredicate;
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

    public List<Roles> getRoles() {
        return roles;
    }

    public Predicate<CaseData> getCompletedPredicate() {
        return completedPredicate;
    }
}
