package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfProceedings {
    @CCD(
            label = "What would you like to create?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ProceedingType"
    )
    private final List<ProceedingType> proceedingTypes;
    @CCD(label = "Judge and Justices' Legal Adviser")
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;

    @JsonIgnore
    public List<DocmosisTemplates> mapProceedingTypesToDocmosisTemplate() {
        if (proceedingTypes == null) {
            return List.of();
        }

        return proceedingTypes.stream()
            .map(ProceedingType::getTemplate)
            .collect(Collectors.toList());
    }
}
