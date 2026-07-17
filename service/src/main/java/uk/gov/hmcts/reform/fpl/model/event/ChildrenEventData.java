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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCrudPlus2RolesUjhfjmAccess;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildrenEventData {
    // page 2
    @CCD(
            label = "Do you know if any of the children have legal representation?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess.class}
    )
    String childrenHaveRepresentation;
    @CCD(
            label = " ",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerApproverCruAccess.class}
    )
    @Temp
    RespondentSolicitor childrenMainRepresentative;

    // page 3
    @CCD(
            label = "Do all the children have this Cafcass legal representative?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerApproverCruAccess.class}
    )
    @Temp
    String childrenHaveSameRepresentation;
    @CCD(
            label = "Child 1",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails0;
    @CCD(
            label = "Child 2",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails1;
    @CCD(
            label = "Child 3",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails2;
    @CCD(
            label = "Child 4",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails3;
    @CCD(
            label = "Child 5",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails4;
    @CCD(
            label = "Child 6",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails5;
    @CCD(
            label = "Child 7",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails6;
    @CCD(
            label = "Child 8",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails7;
    @CCD(
            label = "Child 9",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails8;
    @CCD(
            label = "Child 10",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails9;
    @CCD(
            label = "Child 11",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails10;
    @CCD(
            label = "Child 12",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails11;
    @CCD(
            label = "Child 13",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails12;
    @CCD(
            label = "Child 14",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
    @Temp
    ChildRepresentationDetails childRepresentationDetails13;
    @CCD(
            label = "Child 15",
            searchable = false,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerApproverCrudPlus2RolesUjhfjmAccess.class}
    )
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
        } else if (YesNo.YES.getValue().equals(childrenHaveSameRepresentation)) {
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
