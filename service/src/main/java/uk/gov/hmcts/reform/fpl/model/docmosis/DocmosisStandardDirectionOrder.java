package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Data
@Builder(builderClassName = "Builder")
public class DocmosisStandardDirectionOrder {
    private final DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final String courtName;
    private final String familyManCaseNumber;
    private final String dateOfIssue;
    private final String complianceDeadline;
    private final List<DocmosisRespondent> respondents;
    private final List<DocmosisChildren> children;
    private final boolean respondentsProvided;
    private final String applicantName;
    private final DocmosisHearingBooking hearingBooking;
    private final List<DocmosisDirection> directions;
    private final String draftbackground;
    private final String courtseal;

    public Map<String, Object> toMap(ObjectMapper mapper) {
        Map<String, Object> map = mapper.convertValue(this, new TypeReference<>() {});

        if (isNotEmpty(this.directions)) {
            map.putAll(this.directions.stream().collect(groupingBy(direction -> direction.assignee.getValue())));
        }

        map.remove("directions");

        return map;
    }
}
