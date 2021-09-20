package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class DocmosisOrder implements DocmosisData {
    private final DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final String courtName;
    private final String familyManCaseNumber;
    private final String ccdCaseNumber;
    private final String dateOfIssue;
    private final List<DocmosisChild> children;
    private final List<DocmosisDirection> directions;
    private final String draftbackground;
    private final String courtseal;
    private final String crest;

    @Override
    public Map<String, Object> toMap(ObjectMapper mapper) {
        Map<String, Object> map = mapper.convertValue(this, new TypeReference<>() {});

        if (isNotEmpty(directions)) {
            map.putAll(directions.stream().collect(groupingBy(direction -> direction.assignee.getValue())));
        }

        map.remove("directions");

        return map;
    }
}
