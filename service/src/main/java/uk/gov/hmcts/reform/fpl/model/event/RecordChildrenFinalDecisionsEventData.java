package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.children.ChildFinalDecisionDetails;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordChildrenFinalDecisionsEventData {
    @PastOrPresent(message = "The close case date must be in the past",
        groups = RecordChildrenFinalDecisionsEventData.class)
    LocalDate finalDecisionDate;

    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails00;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails01;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails02;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails03;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails04;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails05;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails06;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails07;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails08;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails09;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails10;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails11;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails12;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails13;
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails14;

    @JsonIgnore
    public List<ChildFinalDecisionDetails> getAllChildrenDecisionDetails() {
        List<ChildFinalDecisionDetails> childFinalDecisionDetails = new ArrayList<>();

        childFinalDecisionDetails.add(childFinalDecisionDetails00);
        childFinalDecisionDetails.add(childFinalDecisionDetails01);
        childFinalDecisionDetails.add(childFinalDecisionDetails02);
        childFinalDecisionDetails.add(childFinalDecisionDetails03);
        childFinalDecisionDetails.add(childFinalDecisionDetails04);
        childFinalDecisionDetails.add(childFinalDecisionDetails05);
        childFinalDecisionDetails.add(childFinalDecisionDetails06);
        childFinalDecisionDetails.add(childFinalDecisionDetails07);
        childFinalDecisionDetails.add(childFinalDecisionDetails08);
        childFinalDecisionDetails.add(childFinalDecisionDetails09);
        childFinalDecisionDetails.add(childFinalDecisionDetails10);
        childFinalDecisionDetails.add(childFinalDecisionDetails11);
        childFinalDecisionDetails.add(childFinalDecisionDetails12);
        childFinalDecisionDetails.add(childFinalDecisionDetails13);
        childFinalDecisionDetails.add(childFinalDecisionDetails14);

        return childFinalDecisionDetails;
    }

    @JsonIgnore
    public String[] getTransientFields() {
        List<String> fields =
            getFieldsListWithAnnotation(RecordChildrenFinalDecisionsEventData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(Collectors.toList());

        fields.addAll(List.of("optionCount", "close_case_label", "children_label", "orderAppliesToAllChildren",
            "finalDecisionDate"));

        return fields.toArray(String[]::new);
    }
}
