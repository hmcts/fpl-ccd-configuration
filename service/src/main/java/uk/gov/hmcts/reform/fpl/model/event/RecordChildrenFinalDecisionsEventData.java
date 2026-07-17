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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordChildrenFinalDecisionsEventData {
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @PastOrPresent(message = "The close case date must be in the past",
        groups = RecordChildrenFinalDecisionsEventData.class)
    LocalDate finalDecisionDate;

    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails00;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails01;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails02;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails03;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails04;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails05;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails06;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails07;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails08;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails09;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails10;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails11;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails12;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    ChildFinalDecisionDetails childFinalDecisionDetails13;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
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
