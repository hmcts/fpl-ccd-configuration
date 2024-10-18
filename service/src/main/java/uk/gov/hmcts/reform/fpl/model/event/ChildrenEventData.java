package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildrenEventData {
    // page 2
    String childrenHaveRepresentation;
    @Temp
    RespondentSolicitor childrenMainRepresentative;

    // page 3
    @Temp
    String childrenHaveSameRepresentation;
    @Temp
    ChildRepresentationDetails childRepresentationDetails0;
    @Temp
    ChildRepresentationDetails childRepresentationDetails1;
    @Temp
    ChildRepresentationDetails childRepresentationDetails2;
    @Temp
    ChildRepresentationDetails childRepresentationDetails3;
    @Temp
    ChildRepresentationDetails childRepresentationDetails4;
    @Temp
    ChildRepresentationDetails childRepresentationDetails5;
    @Temp
    ChildRepresentationDetails childRepresentationDetails6;
    @Temp
    ChildRepresentationDetails childRepresentationDetails7;
    @Temp
    ChildRepresentationDetails childRepresentationDetails8;
    @Temp
    ChildRepresentationDetails childRepresentationDetails9;
    @Temp
    ChildRepresentationDetails childRepresentationDetails10;
    @Temp
    ChildRepresentationDetails childRepresentationDetails11;
    @Temp
    ChildRepresentationDetails childRepresentationDetails12;
    @Temp
    ChildRepresentationDetails childRepresentationDetails13;
    @Temp
    ChildRepresentationDetails childRepresentationDetails14;

    @JsonIgnore
    public List<ChildRepresentationDetails> getAllRepresentationDetails() {
        // mutable to allow null values
        List<ChildRepresentationDetails> childRepresentationDetails = new ArrayList<>();
        childRepresentationDetails.add(childRepresentationDetails0);
        childRepresentationDetails.add(childRepresentationDetails1);
        childRepresentationDetails.add(childRepresentationDetails2);
        childRepresentationDetails.add(childRepresentationDetails3);
        childRepresentationDetails.add(childRepresentationDetails4);
        childRepresentationDetails.add(childRepresentationDetails5);
        childRepresentationDetails.add(childRepresentationDetails6);
        childRepresentationDetails.add(childRepresentationDetails7);
        childRepresentationDetails.add(childRepresentationDetails8);
        childRepresentationDetails.add(childRepresentationDetails9);
        childRepresentationDetails.add(childRepresentationDetails10);
        childRepresentationDetails.add(childRepresentationDetails11);
        childRepresentationDetails.add(childRepresentationDetails12);
        childRepresentationDetails.add(childRepresentationDetails13);
        childRepresentationDetails.add(childRepresentationDetails14);
        return childRepresentationDetails;
    }

    @JsonIgnore
    public String[] getTransientFields() {
        List<String> fields = new ArrayList<>();
        if (!YesNo.YES.getValue().equals(childrenHaveRepresentation)) {
            fields = getFieldsListWithAnnotation(ChildrenEventData.class, Temp.class).stream()
                .map(Field::getName)
                .collect(Collectors.toList());
        }

        if (YesNo.YES.getValue().equals(childrenHaveSameRepresentation)) {
            List<String> excludedFields = List.of("childrenMainRepresentative", "childrenHaveSameRepresentation");
            fields = getFieldsListWithAnnotation(ChildrenEventData.class, Temp.class).stream()
                .map(Field::getName)
                .filter(field -> !excludedFields.contains(field))
                .collect(Collectors.toList());
        }

        fields.add(OptionCountBuilder.CASE_FIELD);

        return fields.toArray(String[]::new);
    }
}
