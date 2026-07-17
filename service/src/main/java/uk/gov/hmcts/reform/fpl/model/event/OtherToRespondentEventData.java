package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherToRespondentEventData {

    @CCD(
            label = "Select one of the others to be given notice",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawGatekeeperRPlus2RolesDnipijAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList othersList;

    @CCD(label = "Respondent's detail", searchable = false, access = {CaseworkerPubliclawCourtadminCruAccess.class})
    @Temp
    private final Respondent transformedRespondent;

}
