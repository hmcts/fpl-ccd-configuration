package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementSupportingDocument {
    private PlacementDocumentType type;
    private DocumentReference document;
    private String description;

    public enum PlacementDocumentType {
        BIRTH_ADOPTION_CERTIFICATE,
        PARENTS_CONSENT_FOR_ADOPTION,
        WITHDRAWAL_OF_PARENT_CONSENT,
        STATEMENT_OF_FACTS,
        FINAL_CARE_ORDER,
        PARENTAL_RESPONSIBILITY_ORDER_AGREEMENT,
        OTHER_FINAL_ORDERS,
        MAINTENANCE_AGREEMENT_AWARD,
        FINAL_REPORTS_RELATING_TO_SIBLINGS
    }
}
