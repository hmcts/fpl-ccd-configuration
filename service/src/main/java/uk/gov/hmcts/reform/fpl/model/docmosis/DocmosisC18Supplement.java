package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisC18Supplement implements DocmosisData {
    private final String welshLanguageRequirement;
    private final String courtName;
    private final String childrenNames;
    private final String submittedDate;
    private String caseNumber;

    private String courtSeal;
    private String draftWaterMark;

    private String childOrChildren;
    private String isOrAre;

    private final List<String> particularsOfChildren;
    private final String particularsOfChildrenDetails;
    private final String directionsAppliedFor;
    private final List<String> grounds;
    private final String reason;
}
