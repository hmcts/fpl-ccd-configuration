package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationRouteType;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadAdditionalApplicationsEventData {
    @Temp
    C2DocumentBundle temporaryC2Document;
    @Temp
    C2ApplicationRouteType c2ApplicationRoute;
    @Temp
    Map<String, C2ApplicationType> c2ApplicationType;
    @Temp
    C2ApplicationType c2Type;
    @Temp
    YesNo isC2Confidential;
    @Temp
    List<AdditionalApplicationType> additionalApplicationType;
    @Temp
    PBAPayment temporaryPbaPayment;
    @Temp
    DocumentReference c2EvidenceConsentDocument;
    @Temp
    OtherApplicationsBundle temporaryOtherApplicationsBundle;
    @Temp
    DynamicList applicantsList;
    @Temp
    String otherApplicant;

    public List<AdditionalApplicationType> getAdditionalApplicationType() {
        return defaultIfNull(additionalApplicationType, emptyList());
    }
}
