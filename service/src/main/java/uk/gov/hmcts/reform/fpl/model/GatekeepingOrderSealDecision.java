package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatekeepingOrderSealDecision {
    private final DocumentReference draftDocument;
    private final LocalDate dateOfIssue;
    private final OrderStatus orderStatus;
    private final String nextSteps;
    private final LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    public boolean isSealed() {
        return orderStatus == SEALED;
    }
}
