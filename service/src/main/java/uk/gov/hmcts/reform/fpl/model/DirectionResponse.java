package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class DirectionResponse {
    private final UUID directionId;
    private final DirectionAssignee assignee;
    private final String complied;
    private final String documentDetails;
    private final DocumentReference file;
    private final String cannotComplyReason;
    private final List<String> c2Uploaded;
    private final DocumentReference cannotComplyFile;
    @JsonIgnore
    private String respondingOnBehalfOf;

    @JsonGetter("respondingOnBehalfOfRespondent")
    private String getRespondingOnBehalfOfRespondent() {
        if (respondingOnBehalfOf != null && respondingOnBehalfOf.contains("RESPONDENT")) {
            return respondingOnBehalfOf;
        }
        return null;
    }

    @JsonGetter("respondingOnBehalfOfOthers")
    private String getRespondingOnBehalfOfOthers() {
        if (respondingOnBehalfOf != null && respondingOnBehalfOf.contains("OTHER")) {
            return respondingOnBehalfOf;
        }
        return null;
    }

    @JsonGetter("respondingOnBehalfOfCafcass")
    private String getRespondingOnBehalfOfCafcass() {
        if (respondingOnBehalfOf != null && respondingOnBehalfOf.contains("CAFCASS")) {
            return respondingOnBehalfOf;
        }
        return null;
    }

    @JsonSetter("respondingOnBehalfOfRespondent")
    private void setRespondingOnBehalfOfRespondent(String value) {
        if (value != null) {
            respondingOnBehalfOf = value;
        }
    }

    @JsonSetter("respondingOnBehalfOfOthers")
    private void setRespondingOnBehalfOfOthers(String value) {
        if (value != null) {
            respondingOnBehalfOf = value;
        }
    }

    @JsonSetter("respondingOnBehalfOfCafcass")
    private void setRespondingOnBehalfOfCafcass(String value) {
        if (value != null) {
            respondingOnBehalfOf = value;
        }
    }
}
