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

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalAuthoritiesEventData {

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToShare;

    @Temp
    private final LocalAuthorityAction localAuthorityAction;

    @Temp
    private final String localAuthorityEmail;

    @Temp
    private final String localAuthorityToRemove;

    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo transferToSharedLocalAuthority;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToTransfer;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList localAuthoritiesToTransferWithoutShared;

    @Temp
    private final String sharedLocalAuthority;

    @Temp
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo transferToCourt;

    @Temp
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList courtsToTransfer;

    @Temp
    private final String currentCourtName;

    @Temp
    private final LocalAuthority localAuthorityToTransfer;

    @Temp
    private final Colleague localAuthorityToTransferSolicitor;

}
