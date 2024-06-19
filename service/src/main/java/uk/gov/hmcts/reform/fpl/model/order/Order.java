package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPOINTED_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CAFCASS_JURISDICTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_ASSESSMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_PLACEMENT_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CHILD_PLACEMENT_FOR_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CLOSE_CASE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DECLARATION_OF_PARENTAGE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DISCHARGE_DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EDUCATION_SUPERVISION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_CHILDREN_DESCRIPTION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_INCLUDE_PHRASE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_TYPE_AND_PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FURTHER_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ICO_EXCLUSION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.IS_CHILD_REPRESENTED;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LEAVE_TO_CHANGE_CHILD_SURNAME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LINKED_TO_HEARING;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LINK_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_MONTH;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.NEED_SEALING;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.NON_MOLESTATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_BY_CONSENT;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_PLACED_CHILD_IN_CUSTODY;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_TO_AMEND;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.PARTY_ALLOWED_CONTACTS_AND_CONDITIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.REASON_FOR_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.REFUSE_CONTACT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.RESPONDENTS_REFUSED;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.REVIEW_DRAFT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SECURE_ACCOMMODATION_ORDER_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SELECT_SINGLE_CHILD;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.TITLE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.TRANSLATION_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.UPLOAD_AMENDED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.UPLOAD_ORDER_FILE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.VARY_OR_EXTEND_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSourceType.AMENDED;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSourceType.DIGITAL;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSourceType.MANUAL_UPLOAD;

@Getter
@RequiredArgsConstructor
public enum Order {
    A70_PLACEMENT_ORDER(
        DIGITAL,
        "Placement Order",
        "Section 21 of the Adoption and Children Act 2002",
        "Placement Order (A70)",
        IsFinalOrder.MAYBE,
        List.of(CHILD_PLACEMENT_APPLICATIONS, APPROVER, APPROVAL_DATE,
            CHILD_PLACEMENT,
            CLOSE_CASE,
            REVIEW_DRAFT_ORDER)
    ),
    A81_PLACEMENT_BLANK_ORDER(
        DIGITAL,
        "Placement order",
        "Section 21 of the Adoption and Children Act 2002",
        "Placement order (A81)",
        IsFinalOrder.MAYBE,
        List.of(CHILD_PLACEMENT_APPLICATIONS, APPROVER, APPROVAL_DATE,
            CHILD_PLACEMENT_FOR_BLANK_ORDER,
            REVIEW_DRAFT_ORDER)
    ),
    C21_BLANK_ORDER(
        DIGITAL,
        "Blank order",
        "Section 31 Children Act 1989",
        "Blank order (C21)",
        IsFinalOrder.NO,
        List.of(LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN,
            TITLE, DETAILS, REVIEW_DRAFT_ORDER)
    ),
    C23_EMERGENCY_PROTECTION_ORDER(
        DIGITAL,
        "Emergency protection order",
        "Section 44 Children Act 1989",
        "Emergency protection order (C23)",
        IsFinalOrder.MAYBE,
        List.of(LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE_TIME, WHICH_CHILDREN,
            EPO_TYPE_AND_PREVENT_REMOVAL, EPO_INCLUDE_PHRASE, EPO_CHILDREN_DESCRIPTION, EPO_EXPIRY_DATE,
            FURTHER_DIRECTIONS, REVIEW_DRAFT_ORDER, CLOSE_CASE)
    ),
    C26_SECURE_ACCOMMODATION_ORDER(
        DIGITAL,
        "Authority to keep a child in secure accommodation",
        "",
        "Authority to keep a child in secure accommodation (C26)",
        IsFinalOrder.MAYBE,
        List.of(LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE_TIME, SELECT_SINGLE_CHILD,
            ORDER_BY_CONSENT, REASON_FOR_SECURE_ACCOMMODATION, IS_CHILD_REPRESENTED,
            SECURE_ACCOMMODATION_ORDER_JURISDICTION, MANAGE_ORDER_END_DATE_WITH_MONTH, FURTHER_DIRECTIONS,
            REVIEW_DRAFT_ORDER, CLOSE_CASE)
    ),
    C29_RECOVERY_OF_A_CHILD(
        DIGITAL,
        "Recovery of a child (C29)",
        "Section 50 Children Act 1989",
        "Recovery of a child (C29)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN,
            ORDER_PLACED_CHILD_IN_CUSTODY, FURTHER_DIRECTIONS, CLOSE_CASE, REVIEW_DRAFT_ORDER
        )
    ),
    C32A_CARE_ORDER(
        DIGITAL,
        "Care order",
        "Section 31 Children Act 1989",
        "Care order (C32A)",
        IsFinalOrder.YES,
        List.of(LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS,
            REVIEW_DRAFT_ORDER, CLOSE_CASE)
    ),
    C32B_DISCHARGE_OF_CARE_ORDER(
        DIGITAL,
        "Discharge of care order",
        "Section 39(1) Children Act 1989",
        "Discharge of care order (C32B)",
        IsFinalOrder.MAYBE,
        List.of(LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, DISCHARGE_DETAILS,
            FURTHER_DIRECTIONS, CLOSE_CASE, REVIEW_DRAFT_ORDER)
    ),
    C35A_SUPERVISION_ORDER(
        DIGITAL,
        "Supervision order",
        "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
        "Supervision order (C35A)",
        IsFinalOrder.YES,
        List.of(
            LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS,
            MANAGE_ORDER_END_DATE_WITH_MONTH, REVIEW_DRAFT_ORDER, CLOSE_CASE, REVIEW_DRAFT_ORDER)
    ),
    C33_INTERIM_CARE_ORDER(
        DIGITAL,
        "Interim care order",
        "Section 38 Children Act 1989",
        "Interim care order (C33)",
        IsFinalOrder.NO,
        List.of(
            LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, ICO_EXCLUSION,
            FURTHER_DIRECTIONS, MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS, REVIEW_DRAFT_ORDER)
    ),
    C39_CHILD_ASSESSMENT_ORDER(
        DIGITAL,
        "Child assessment order",
        "Section 43 Children Act 1989",
        "Child assessment order (C39)",
        IsFinalOrder.MAYBE,
        List.of(
            APPROVER,
            APPROVAL_DATE,
            SELECT_SINGLE_CHILD,
            CHILD_ASSESSMENT_ORDER,
            ORDER_BY_CONSENT,
            REVIEW_DRAFT_ORDER,
            CLOSE_CASE)
    ),
    C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER(
        DIGITAL,
        "Child arrangements, Specific issue, Prohibited steps",
        "Section 8 Children Act 1989",
        "Child arrangements, Specific issue, Prohibited steps (C43)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, ORDER_BY_CONSENT,
            DETAILS, FURTHER_DIRECTIONS, CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS, CLOSE_CASE,
            REVIEW_DRAFT_ORDER)
    ),
    C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN(
        DIGITAL,
        "Appointment of a children's guardian",
        "Section 41(1) Children Act 1989",
        "Appointment of a children's guardian (C47A)",
        IsFinalOrder.NO,
        List.of(
            LINKED_TO_HEARING, LINK_APPLICATION, APPROVER, APPROVAL_DATE, CAFCASS_JURISDICTIONS, FURTHER_DIRECTIONS,
            REVIEW_DRAFT_ORDER)
    ),
    C35B_INTERIM_SUPERVISION_ORDER(
        DIGITAL,
        "Interim supervision order",
        "Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
        "Interim supervision order (C35B)",
        IsFinalOrder.NO,
        List.of(
            LINKED_TO_HEARING,
            LINK_APPLICATION,
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            FURTHER_DIRECTIONS,
            MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS,
            REVIEW_DRAFT_ORDER)
    ),
    C43A_SPECIAL_GUARDIANSHIP_ORDER(
        DIGITAL,
        "Special guardianship order",
        "Section 14A(1) Children Act 1989",
        "Special guardianship order (C43A)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING,
            LINK_APPLICATION,
            APPROVER,
            APPROVAL_DATE_TIME,
            WHICH_CHILDREN,
            ORDER_BY_CONSENT,
            APPOINTED_GUARDIAN,
            FURTHER_DIRECTIONS,
            REVIEW_DRAFT_ORDER,
            CLOSE_CASE
        )
    ),
    C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS(
        DIGITAL,
        "Variation or extension of education supervision orders (C36)",
        "Sections 39(2), 39(3) and 39(4) and Paragraph 6(3) Schedule 3 Children Act 1989",
        "Variation or extension of education supervision orders (C36)",
        IsFinalOrder.MAYBE,
        List.of(
            APPROVER,
            APPROVAL_DATE,
            VARY_OR_EXTEND_SUPERVISION_ORDER,
            ORDER_BY_CONSENT,
            WHICH_CHILDREN,
            REVIEW_DRAFT_ORDER
        )
    ),
    C44A_LEAVE_TO_CHANGE_A_SURNAME(
        DIGITAL,
        "Leave to change the surname by which a child is known",
        "Section [13(1)] [14C(3)] [33(7)] Children Act 1989",
        "Leave to change the surname by which a child is known",
        IsFinalOrder.MAYBE,
        List.of(
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            LEAVE_TO_CHANGE_CHILD_SURNAME,
            ORDER_BY_CONSENT,
            REVIEW_DRAFT_ORDER
        )
    ),

    /* MANUAL UPLOADS */
    C24_VARIATION_OF_EMERGENCY_PROTECTION_ORDER(
        MANUAL_UPLOAD,
        "Variation of Emergency protection order (C24)",
        "",
        "Variation of Emergency protection order (C24)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C25_WARRANT_TO_ASSIST_EPO(
        MANUAL_UPLOAD,
        "Warrant to assist EPO (C25)",
        "",
        "Warrant to assist EPO (C25)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C28_WARRANT_TO_ASSIST(
        MANUAL_UPLOAD,
        "Warrant to assist (C28)",
        "",
        "Warrant to assist (C28)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C27_AUTHORITY_TO_SEARCH_FOR_ANOTHER_CHILD(
        MANUAL_UPLOAD,
        "Authority to search for another child (C27)",
        "",
        "Authority to search for another child (C27)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C30_TO_DISCLOSE_INFORMATION_ABOUT_THE_WHEREABOUTS_OF_A_MISSING_CHILD(
        MANUAL_UPLOAD,
        "To disclose information about the whereabouts of a missing child (C30)",
        "",
        "To disclose information about the whereabouts of a missing child (C30)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C31_AUTHORITY_TO_SEARCH_FOR_A_CHILD(
        MANUAL_UPLOAD,
        "Authority to search for a child (C31)",
        "",
        "Authority to search for a child (C31)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C34A_CONTACT_WITH_A_CHILD_IN_CARE(
        DIGITAL,
        "Contact with a child in care (C34A)",
        "Sections 34(2) and (3) Children Act 1989",
        "Contact with a child in care (C34A)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING,
            LINK_APPLICATION,
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            PARTY_ALLOWED_CONTACTS_AND_CONDITIONS,
            ORDER_BY_CONSENT,
            CLOSE_CASE,
            REVIEW_DRAFT_ORDER
        )
    ),
    C34B_AUTHORITY_TO_REFUSE_CONTACT(
        DIGITAL,
        "Authority to refuse contact with a child in care (C34B)",
        "",
        "Authority to refuse contact with a child in care (C34B)",
        IsFinalOrder.MAYBE,
        List.of(ORDER_BY_CONSENT, REFUSE_CONTACT_ORDER, RESPONDENTS_REFUSED,
            APPROVER, APPROVAL_DATE, WHICH_CHILDREN, CLOSE_CASE, REVIEW_DRAFT_ORDER)
    ),
    C37_EDUCATION_SUPERVISION_ORDER_DIGITAL(
        DIGITAL,
        "Education supervision order (C37)",
        "Section 36 Children Act 1989",
        "Education supervision order (C37)",
        IsFinalOrder.MAYBE,
        List.of(
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            EDUCATION_SUPERVISION,
            ORDER_BY_CONSENT,
            REVIEW_DRAFT_ORDER,
            CLOSE_CASE)
    ),
    C38A_DISCHARGE_EDUCATION_SUPERVISION_ORDER(
        MANUAL_UPLOAD,
        "Discharge education supervision order (C38A)",
        "",
        "Discharge education supervision order (C38A)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C38B_EXTENSION_OF_AN_EDUCATION_SUPERVISION_ORDER(
        MANUAL_UPLOAD,
        "Extension of an education supervision order (C38B)",
        "",
        "Extension of an education supervision order (C38B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C42_FAMILY_ASSISTANCE_ORDER(
        DIGITAL,
        "Family assistance order (C42)",
        "Section 16 Children Act 1989",
        "Family assistance order (C42)",
        IsFinalOrder.MAYBE,
        List.of(
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            FAMILY_ASSISTANCE_ORDER,
            FURTHER_DIRECTIONS,
            ORDER_BY_CONSENT,
            REVIEW_DRAFT_ORDER,
            CLOSE_CASE
        )
    ),
    C44B_LEAVE_TO_REMOVE_A_CHILD_FROM_THE_UK(
        MANUAL_UPLOAD,
        "Leave to remove a child from the UK (C44B)",
        "",
        "Leave to remove a child from the UK (C44B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C45A_PARENTAL_RESPONSIBILITY_ORDER(
        DIGITAL,
        "Parental responsibility order (C45A)",
        "",
        "Parental responsibility order (C45A)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING,
            LINK_APPLICATION,
            APPROVER,
            APPROVAL_DATE_TIME,
            WHICH_CHILDREN,
            ORDER_BY_CONSENT,
            PARENTAL_RESPONSIBILITY,
            CLOSE_CASE,
            FURTHER_DIRECTIONS,
            REVIEW_DRAFT_ORDER
        )
    ),
    C45B_DISCHARGE_OF_PARENTAL_RESPONSIBILITY(
        MANUAL_UPLOAD,
        "Discharge of parental responsibility (C45B)",
        "",
        "Discharge of parental responsibility (C45B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C46A_APPOINTMENT_OF_A_GUARDIAN(
        MANUAL_UPLOAD,
        "Appointment of a guardian (C46A)",
        "",
        "Appointment of a guardian (C46A)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C46B_TERMINATION_OF_GUARDIANS_APPOINTMENT(
        MANUAL_UPLOAD,
        "Termination of guardian's appointment (C46B)",
        "",
        "Termination of guardian's appointment (C46B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C47B_REFUSAL_OF_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN(
        MANUAL_UPLOAD,
        "Refusal of appointment of a children's guardian (C47B)",
        "",
        "Refusal of appointment of a children's guardian (C47B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C47C_TERMINATION_OF_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN(
        MANUAL_UPLOAD,
        "Termination of appointment of a children's guardian (C47C)",
        "",
        "Termination of appointment of a children's guardian (C47C)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C48A_APPOINTMENT_OF_A_SOLICITOR(
        MANUAL_UPLOAD,
        "Appointment of a solicitor (C48A)",
        "",
        "Appointment of a solicitor (C48A)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C48B_REFUSAL_OF_APPOINTMENT_OF_A_SOLICITOR(
        MANUAL_UPLOAD,
        "Refusal of appointment of a solicitor (C48B)",
        "",
        "Refusal of appointment of a solicitor (C48B)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C48C_TERMINATION_OF_APPOINTMENT_OF_A_SOLICITOR(
        MANUAL_UPLOAD,
        "Termination of appointment of a solicitor (C48C)",
        "",
        "Termination of appointment of a solicitor (C48C)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C49_TRANSFER_OUT_CHILDREN_ACT(
        MANUAL_UPLOAD,
        "Transfer out Children Act (C49)",
        "",
        "Transfer out Children Act (C49)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C50_REFUSAL_TO_TRANSFER_PROCEEDINGS(
        MANUAL_UPLOAD,
        "Refusal to transfer proceedings (C50)",
        "",
        "Refusal to transfer proceedings (C50)",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    C63A_DECLARATION_OF_PARENTAGE(
        DIGITAL,
        "Declaration of parentage (C63A)",
        "Section 55A of the Family Law Act 1986",
        "Declaration of parentage (C63A)",
        IsFinalOrder.MAYBE,
        List.of(
            LINKED_TO_HEARING,
            LINK_APPLICATION,
            APPROVER,
            APPROVAL_DATE,
            SELECT_SINGLE_CHILD,
            DECLARATION_OF_PARENTAGE,
            CLOSE_CASE,
            REVIEW_DRAFT_ORDER
        )
    ),
    FL404A_NON_MOLESTATION_ORDER(
        DIGITAL,
        "Non-molestation order (FL404A)",
        "Section 42 Family Law Act 1996",
        "Non-molestation order (FL404A)",
        IsFinalOrder.NO,
        List.of(
            LINKED_TO_HEARING,
            APPROVER,
            APPROVAL_DATE,
            WHICH_CHILDREN,
            NON_MOLESTATION_ORDER,
            ORDER_BY_CONSENT,
            REVIEW_DRAFT_ORDER
        )
    ),
    FL406_POWER_OF_ARREST(
        MANUAL_UPLOAD,
        "",
        "",
        "",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    OTHER_ORDER(
        MANUAL_UPLOAD,
        "",
        "",
        "Other",
        IsFinalOrder.NO,
        Constants.MANUAL_UPLOAD_QUESTIONS
    ),
    // although this isn't an order, by defining this we get to take advantage of the existing framework
    AMENED_ORDER(
        AMENDED,
        "",
        "",
        "",
        IsFinalOrder.NO,
        List.of(ORDER_TO_AMEND, UPLOAD_AMENDED_ORDER)
    );

    private final OrderSourceType sourceType;
    private final String title;
    private final String childrenAct;
    private final String historyTitle;
    private final IsFinalOrder isFinalOrder;
    private final List<OrderQuestionBlock> questionsBlocks;

    public String fileName(RenderFormat format) {
        return String.format("%s.%s", this.name().toLowerCase(), format.getExtension());
    }

    public String fileName(RenderFormat format, ManageOrdersEventData manageOrdersEventData) {
        if (C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER.equals(this)) {
            String c43Orders = manageOrdersEventData.getManageOrdersMultiSelectListForC43()
                .stream().map(C43OrderType -> C43OrderType.getLabel().toLowerCase().replace(" ", "_"))
                .collect(Collectors.joining("_"));

            return String.format("c43_%s.%s", c43Orders, format.getExtension());
        }

        return fileName(format);
    }

    public OrderSection firstSection() {
        return this.getQuestionsBlocks().get(0).getSection();
    }

    public Optional<OrderSection> nextSection(OrderSection currentSection) {
        Set<OrderSection> sectionsForOrder = this.getQuestionsBlocks()
            .stream()
            .map(OrderQuestionBlock::getSection)
            .collect(Collectors.toSet());

        for (int i = 0; i < OrderSection.values().length - 1; i++) {
            if (currentSection.equals(OrderSection.values()[i])) { // current section found
                for (int j = i + 1; j < OrderSection.values().length; j++) { // assume sections in order
                    if (sectionsForOrder.contains(OrderSection.values()[j])) { // question sections contain section
                        return Optional.of(OrderSection.values()[j]);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static class Constants {
        private static final List<OrderQuestionBlock> MANUAL_UPLOAD_QUESTIONS = List.of(
            APPROVAL_DATE,
            WHICH_CHILDREN,
            UPLOAD_ORDER_FILE,
            NEED_SEALING,
            REVIEW_DRAFT_ORDER,
            CLOSE_CASE,
            TRANSLATION_REQUIREMENTS
        );
    }
}
