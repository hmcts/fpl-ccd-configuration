package uk.gov.hmcts.reform.fpl.model.order;

import java.util.List;

public enum OrderQuestionBlock {
    APPROVER("approver", "Approver", OrderSection.SECTION_2,
        List.of("judgeAndLegalAdvisor")),
    APPROVAL_DATE("approvalDate", "Approval Date", OrderSection.SECTION_2,
        List.of("manageOrdersApprovalDate")),
    WHICH_CHILDREN("whichChildren", "Which children", OrderSection.SECTION_3,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    FURTHER_DIRECTIONS("furtherDirections", "Further Directions", OrderSection.SECTION_4,
        List.of("manageOrdersFurtherDirections"));


    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;

    OrderQuestionBlock(String showHideField, String question, OrderSection section, List<String> dataFields) {
        this.showHideField = showHideField;
        this.question = question;
        this.section = section;
        this.dataFields = dataFields;
    }

    public String getQuestion() {
        return question;
    }

    public OrderSection getSection() {
        return section;
    }


    public String getShowHideField() {
        return showHideField;
    }

    public List<String> getDataFields() {
        return dataFields;
    }
}
