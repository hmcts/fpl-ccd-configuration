package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisC14Supplement implements DocmosisData {
    private final String welshLanguageRequirement;
    private final String courtName;
    private final String childrensNames;
    private final String submittedDate;
    private String caseNumber;

    private String courtSeal;
    private String draftWaterMark;

    private final String personHasContactAndCurrentArrangement;
    private final String laHasRefusedContact;
    private final String personsBeingRefusedContactWithChild;
    private final String reasonsOfApplication;
}
