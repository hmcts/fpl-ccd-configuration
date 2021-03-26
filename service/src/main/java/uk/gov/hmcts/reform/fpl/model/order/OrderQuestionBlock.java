package uk.gov.hmcts.reform.fpl.model.order;

import java.util.List;

public enum OrderQuestionBlock {
    QUESTION_BLOCK_A("questionBlock1","Question Block 1",OrderSection.SECTION_2, List.of("manageOrderQuestion1")),
    QUESTION_BLOCK_B("questionBlock2","Question Block 2",OrderSection.SECTION_2, List.of("manageOrderQuestion2")),
    QUESTION_BLOCK_C("questionBlock3","Question Block 3",OrderSection.SECTION_3, List.of("manageOrderQuestion3"));

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
