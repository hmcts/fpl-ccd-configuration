package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudAccess;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalAuthoritiesEventData {

    @CCD(
            label = "Select local authority",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToShare;

    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final LocalAuthorityAction localAuthorityAction;

    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final LocalAuthorityAction localAuthorityActionLA;

    @CCD(
            label = "LA contact or group inbox email address",
            hint = "You can overwrite a pre-populated address, or add a new one if none is shown",
            searchable = false,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final String localAuthorityEmail;

    @CCD(
            label = "Remove case access from",
            searchable = false,
            access = {LASOLICITORCaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final String localAuthorityToRemove;

    @CCD(
            label = "Is User LA Solicitor",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {LASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo isLaSolicitor;

    @CCD(
            label = "Does the new designated local authority already have case access?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo transferToSharedLocalAuthority;

    @CCD(
            label = "Select local authority to transfer the case to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToTransfer;

    @CCD(
            label = "Select local authority to transfer the case to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToTransferWithoutShared;

    @CCD(label = "xxxx", searchable = false, access = {CaseworkerPubliclawCourtadminCrudAccess.class})
    @Temp
    private final String sharedLocalAuthority;

    @CCD(
            label = "Is the case transferring to a different court",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo transferToCourt;

    @CCD(
            label = "Select new court",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList courtsToTransfer;

    @CCD(label = "Current court", searchable = false, access = {CaseworkerPubliclawCourtadminCrudAccess.class})
    @Temp
    private final String currentCourtName;

    @CCD(
            label = "Local authority details",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final LocalAuthority localAuthorityToTransfer;

    @CCD(
            label = "Local authority solicitor",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    private final Colleague localAuthorityToTransferSolicitor;

    @CCD(label = "Current court", searchable = false, access = {CaseworkerPubliclawCourtadminCrudAccess.class})
    @Temp
    private final String currentCourtNameWithoutTransferLA;

    @CCD(
            label = "Select new court",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList courtsToTransferWithoutTransferLA;

}
