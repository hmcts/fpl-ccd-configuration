package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisC16Supplement implements DocmosisData {
    private final String welshLanguageRequirement;
    private final String courtName;
    private final String childrensNames;
    private final String submittedDate;
    private final String groundsForChildAssessmentOrderReason;
    private final String directionsSoughtAssessment;
    private final String directionsSoughtContact;

    private String caseNumber;
    private String courtSeal;
    private String draftWaterMark;
}
