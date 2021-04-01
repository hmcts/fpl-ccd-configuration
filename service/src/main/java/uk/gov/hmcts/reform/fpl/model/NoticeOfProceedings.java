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

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfProceedings {
    private final List<ProceedingType> proceedingTypes;
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
