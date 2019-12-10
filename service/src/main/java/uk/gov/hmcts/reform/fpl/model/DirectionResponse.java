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
        return getValueIfContainsOrNull("RESPONDENT");
    }

    @JsonGetter("respondingOnBehalfOfOthers")
    private String getRespondingOnBehalfOfOthers() {
        return getValueIfContainsOrNull("OTHER");
    }

    @JsonGetter("respondingOnBehalfOfCafcass")
    private String getRespondingOnBehalfOfCafcass() {
        return getValueIfContainsOrNull("CAFCASS");
    }

    @JsonSetter("respondingOnBehalfOfRespondent")
    private void setRespondingOnBehalfOfRespondent(String value) {
        setValueIfNotNull(value);
    }

    @JsonSetter("respondingOnBehalfOfOthers")
    private void setRespondingOnBehalfOfOthers(String value) {
        setValueIfNotNull(value);
    }

    @JsonSetter("respondingOnBehalfOfCafcass")
    private void setRespondingOnBehalfOfCafcass(String value) {
        setValueIfNotNull(value);
    }

    private String getValueIfContainsOrNull(String value) {
        if (respondingOnBehalfOf != null && respondingOnBehalfOf.contains(value)) {
            return respondingOnBehalfOf;
        }
        return null;
    }

    private void setValueIfNotNull(String value) {
        if (value != null) {
            respondingOnBehalfOf = value;
        }
    }
}
