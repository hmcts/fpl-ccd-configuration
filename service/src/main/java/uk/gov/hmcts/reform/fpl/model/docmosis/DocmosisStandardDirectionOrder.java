package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder(builderClassName = "Builder")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisStandardDirectionOrder {
    private final DocmosisJudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final String courtName;
    private final String familyManCaseNumber;
    private final String generationDate;
    private final String complianceDeadline;
    private final List<DocmosisRespondent> respondents;
    private final List<DocmosisChildren> children;
    private final boolean respondentsProvided;
    private final String applicantName;
    private final DocmosisHearingBooking hearingBooking;
    private final List<DocmosisDirection> directions;
    private final String draftbackground;

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(this, Map.class);
    }
}
