package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ChildExtensionEventData {
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    String childCaseCompletionDateLabel;
    @CCD(label = "Child 1", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension0;
    @CCD(label = "Child 2", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension1;
    @CCD(label = "Child 3", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension2;
    @CCD(label = "Child 4", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension3;
    @CCD(label = "Child 5", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension4;
    @CCD(label = "Child 6", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension5;
    @CCD(label = "Child 7", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension6;
    @CCD(label = "Child 8", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension7;
    @CCD(label = "Child 9", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtension8;
    @CCD(
            label = "Child 10",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension9;
    @CCD(
            label = "Child 11",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension10;
    @CCD(
            label = "Child 12",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension11;
    @CCD(
            label = "Child 13",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension12;
    @CCD(
            label = "Child 14",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension13;
    @CCD(
            label = "Child 15",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    ChildExtension childExtension14;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    ChildExtension childExtensionAll;
    @CCD(
            label = "Select whose timeline is being extended",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    Selector childSelectorForExtension;
    @CCD(
            label = "Is the timeline extending for all the children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    String extensionForAllChildren;
    @CCD(
            label = "Are all the selected children’s timelines being extended by the same amount of time, and for the same reason?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class}
    )
    @Temp
    String sameExtensionForAllChildren;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected0;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected1;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected2;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected3;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected4;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected5;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected6;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected7;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected8;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected9;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected10;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected11;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected12;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected13;
    @CCD(label = " ", searchable = false, access = {CaseworkerPubliclawCourtadminCruPlus3RolesAitoorAccess.class})
    @Temp
    String childSelected14;

    @CCD(
            label = "Was this timeline extension approved at a hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess.class}
    )
    @Temp
    YesNo extendTimelineApprovedAtHearing;
    @CCD(
            label = "When was the order requesting this extension approved?",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess.class}
    )
    @Temp
    LocalDate extendTimelineHearingDate;
    @CCD(
            label = "Which hearing was this extension approved at?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesBetqimAccess.class}
    )
    @Temp
    DynamicList extendTimelineHearingList;

    @JsonIgnore
    public List<ChildExtension> getAllChildExtension() {
        UnaryOperator<ChildExtension> verify = childExtension -> Optional.ofNullable(childExtension)
                .filter(child -> child.getId() != null)
                .orElse(null);

        List<ChildExtension> childExtensions = new ArrayList<>();
        childExtensions.add(verify.apply(childExtension0));
        childExtensions.add(verify.apply(childExtension1));
        childExtensions.add(verify.apply(childExtension2));
        childExtensions.add(verify.apply(childExtension3));
        childExtensions.add(verify.apply(childExtension4));
        childExtensions.add(verify.apply(childExtension5));
        childExtensions.add(verify.apply(childExtension6));
        childExtensions.add(verify.apply(childExtension7));
        childExtensions.add(verify.apply(childExtension8));
        childExtensions.add(verify.apply(childExtension9));
        childExtensions.add(verify.apply(childExtension10));
        childExtensions.add(verify.apply(childExtension11));
        childExtensions.add(verify.apply(childExtension12));
        childExtensions.add(verify.apply(childExtension13));
        childExtensions.add(verify.apply(childExtension14));

        return childExtensions;
    }
}
