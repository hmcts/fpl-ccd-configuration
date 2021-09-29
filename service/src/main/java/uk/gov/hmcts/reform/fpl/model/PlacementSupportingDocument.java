package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementSupportingDocument {
    private Type type;
    private DocumentReference document;
    private String description;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        BIRTH_ADOPTION_CERTIFICATE("Birth/Adoption Certificate"),
        PARENTS_CONSENT_FOR_ADOPTION("Parent's consent for adoption"),
        WITHDRAWAL_OF_PARENT_CONSENT("Withdrawal of parental consent"),
        STATEMENT_OF_FACTS("Statement of facts"),
        FINAL_CARE_ORDER("Final care order"),
        PARENTAL_RESPONSIBILITY_ORDER_AGREEMENT("Parental responsibility order/agreement"),
        OTHER_FINAL_ORDERS("Other final orders"),
        MAINTENANCE_AGREEMENT_AWARD("Maintenance agreement/award"),
        FINAL_REPORTS_RELATING_TO_SIBLINGS("Final reports relating to full, half or step siblings");

        private final String name;


    }
}
